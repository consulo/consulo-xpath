package org.intellij.plugins.xsltDebugger;

import org.intellij.lang.xpath.xslt.run.XsltCommandLineState;
import org.intellij.lang.xpath.xslt.run.XsltRunConfiguration;
import org.intellij.plugins.xsltDebugger.impl.XsltDebugProcess;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

/*
* Created by IntelliJ IDEA.
* User: sweinreuter
* Date: 16.11.10
*/
public class XsltDebuggerRunner extends DefaultProgramRunner
{
	static final ThreadLocal<Boolean> ACTIVE = new ThreadLocal<Boolean>();

	@NonNls
	private static final String ID = "XsltDebuggerRunner";


	@NotNull
	@Override
	public String getRunnerId()
	{
		return ID;
	}

	@Override
	public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile)
	{
		return executorId.equals(DefaultDebugExecutor.EXECUTOR_ID) && profile instanceof XsltRunConfiguration;
	}

	@Nullable
	protected RunContentDescriptor doExecute(@NotNull final RunProfileState runProfileState, @NotNull final ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		Project project = executionEnvironment.getProject();
		final XDebugSession debugSession = XDebuggerManager.getInstance(project).startSession(executionEnvironment, new XDebugProcessStarter()
		{
			@NotNull
			public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException
			{
				ACTIVE.set(Boolean.TRUE);
				try
				{
					final XsltCommandLineState c = (XsltCommandLineState) runProfileState;
					final ExecutionResult result = runProfileState.execute(executionEnvironment.getExecutor(), XsltDebuggerRunner.this);
					return new XsltDebugProcess(session, result, c.getExtensionData().getUserData(XsltDebuggerExtension.VERSION));
				}
				finally
				{
					ACTIVE.remove();
				}
			}
		});
		return debugSession.getRunContentDescriptor();
	}
}