package plugintemplate.standalone

import plugintemplate.PluginProperties
import scala.tools.nsc.CompilerCommand
import scala.tools.nsc.Settings

/** An object for running the plugin as standalone application.
 * 
 *  @todo: print, parse and apply plugin options !!!
 *  ideally re-use the TemplatePlugin (-> runsAfter, optionsHelp,
 *  processOptions, components, annotationChecker) instead of
 *  duplicating it here and in PluginRunner.
 */
object Main {
  def main(args: Array[String]) {
    val settings = new Settings
		
		/* http://groups.google.com/group/simple-build-tool/browse_thread/thread
		 *		/c6324463da33db5c
		 */		
		settings.classpath.value = System.getProperty("java.class.path")		
		settings.bootclasspath.value = System.getProperty("sun.boot.class.path")
		settings.bootclasspath.value += ";lib/scala-library.jar"
		
		// settings.elidebelow.value = 2 * annotation.elidable.ASSERTION
		// settings.noassertions.value = true		
		// println("settings.elidebelow.value = " + settings.elidebelow.value)
			
		// println("user.dir = " + System.getProperty("user.dir"))
		// println("java.class.path = " + System.getProperty("java.class.path"))
		// println("sun.boot.class.path = " + System.getProperty("sun.boot.class.path"))
		
		val infiles = "C:/scmalte/Develop/SymbEx/Implementing/Verifier/src/main/scala/ast/ASTConverter.scala" :: Nil
		
    val command = new CompilerCommand(infiles, settings) {
      /** The command name that will be printed in in the usage message.
       *  This is automatically set to the value of 'plugin.commandname' in the
       *  file build.properties.
       */
      override val cmdName = PluginProperties.pluginCommand
    }

    if (!command.ok)
      return()

    /** The version number of this plugin is read from the properties file
     */
    if (settings.version.value) {
      println(command.cmdName +" version "+ PluginProperties.versionString)
      return()
    }
    if (settings.help.value) {
      println(command.usageMsg)
      return()
    }

    val runner = new PluginRunner(settings)
    val run = new runner.Run
    run.compile(command.files)
  }
}
