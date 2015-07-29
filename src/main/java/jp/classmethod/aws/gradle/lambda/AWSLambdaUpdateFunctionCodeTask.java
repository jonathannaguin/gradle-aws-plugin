/*
 * Copyright 2013-2015 Classmethod, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.classmethod.aws.gradle.lambda;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;

public class AWSLambdaUpdateFunctionCodeTask extends ConventionTask {
	
	@Getter @Setter
	private String functionName;
	
	@Getter @Setter
	private File zipFile;
	
	@Getter
	private UpdateFunctionCodeResult updateFunctionCode;
	
	
	public AWSLambdaUpdateFunctionCodeTask() {
		setDescription("Update Lambda function code.");
		setGroup("AWS");
	}
	
	@TaskAction
	public void updateFunctionCode() throws FileNotFoundException, IOException {
		// to enable conventionMappings feature
		String functionName = getFunctionName();
		
		if (functionName == null)
			throw new GradleException("functionName is required");
		
		AWSLambdaPluginExtension ext = getProject().getExtensions().getByType(AWSLambdaPluginExtension.class);
		AWSLambda lambda = ext.getClient();
		
		try (RandomAccessFile raf = new RandomAccessFile(getZipFile(), "r");
				FileChannel channel = raf.getChannel()) {
			MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			buffer.load();
			UpdateFunctionCodeRequest request = new UpdateFunctionCodeRequest()
				.withFunctionName(getFunctionName())
				.withZipFile(buffer);
			updateFunctionCode = lambda.updateFunctionCode(request);
			getLogger().info("Update Lambda function requested: {}", updateFunctionCode.getFunctionArn());
		}
	}
}
