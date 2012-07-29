import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

/* from http://docs.oracle.com/javase/1.5.0/docs/tooldocs/findingclasses.html
 * 	-> when starting, the JVM searches for and loads classes in the following order
 * 		bootstrap classes ( java platform including rt.jar, classes in jre/lib )
 * 		 -> extension classes
 * 		( jar classes in extension mechanism ) -> classes defined by developers and
 * 		third parties (these are found in the classpath directory ) 
 */

// objects implementing ILaunchConfiguration are immutable
public class 
LocalProfiledApplicationDelegate 
extends AbstractJavaLaunchConfigurationDelegate
{
	// the following code is modelled after the source in 
	//		org.eclipse.jdt.launching.JavaLaunchDelegate
	//		( copyright IBM and all of that )
	@Override
	public void 
	launch
	( 	ILaunchConfiguration configuration, // the config to be launched
		String mode,
		ILaunch launch, 
		IProgressMonitor monitor) 
	throws CoreException 
	{
		// get the name of the class containing the entry point
		String main_type_name
			= super.verifyMainTypeName( configuration );
		System.out.println("Main Type Name: " + main_type_name);
		
		// an IVMRunner is a VMRunner that starts a JVM running a Java program
		IVMRunner runner
			= super.getVMRunner(configuration, mode);
		
		
		File working_directory
			= super.verifyWorkingDirectory( configuration );
		String working_directory_name	
			= null;
		if(working_directory != null){
			working_directory_name
				= working_directory.getAbsolutePath();
		}
		
		String[] environment_variables
			= super.getEnvironment(configuration);
		System.out.println("Environment Variables: ");
		if( environment_variables != null){
			for( String bp : environment_variables ){
				System.out.println(bp);
			}
		}
				
		String program_arguments
			= super.getProgramArguments(configuration);
		System.out.println("Program arguments: " + program_arguments);
		
		String vm_arguments
			= super.getVMArguments( configuration );
		System.out.println("VM Arguments: " + vm_arguments);
		
		ExecutionArguments execution_arguments
			= new ExecutionArguments( vm_arguments, program_arguments);
		
		Map vm_specific_attributes
			= super.getVMSpecificAttributesMap( configuration );
		
		String[] classpath
			= super.getClasspath( configuration );
		System.out.println("Classpath: ");
		if( super.getClasspath(configuration) != null) {
			for( String cp: super.getClasspath(configuration)){
				System.out.println(cp);
			}
		}
		
		// create VM Config
		VMRunnerConfiguration runConfig 
			= new VMRunnerConfiguration( main_type_name, classpath );
		runConfig.setProgramArguments(execution_arguments.getProgramArgumentsArray());
		runConfig.setEnvironment( environment_variables );
		runConfig.setVMArguments( execution_arguments.getVMArgumentsArray() );
		runConfig.setWorkingDirectory( working_directory_name );
		runConfig.setVMSpecificAttributesMap( vm_specific_attributes );
		
		// Bootpath
		runConfig.setBootClassPath( super.getBootpath(configuration));
		System.out.println("Boothpath: ");
		if( super.getBootpath(configuration) != null ){
			for( String bp : super.getBootpath(configuration)){
				System.out.println(bp);
			}
		}
		
		super.prepareStopInMain(configuration);
		super.setDefaultSourceLocator(launch, configuration);
		
		// the following will build a command line and pass it to Runtime.Exec()
		runner.run(runConfig, launch, monitor);
	}
}
