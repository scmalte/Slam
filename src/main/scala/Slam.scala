package de.oakgrove.slam

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class Slam(val global: Global) extends Plugin {
  import global.{CompilationUnit, Traverser, ForeachTreeTraverser, Tree, Apply}
  // import global.definitions._
	
	val name = "slam"
	val description = "Creates class diagrams"

  val runsAfter = List[String]("parser")
  val phaseName = "slam"
	val components = List[PluginComponent](Component)
	
	private object Component extends PluginComponent {
		val global: Slam.this.global.type = Slam.this.global
		val runsAfter = List[String]("parser");
		val phaseName = Slam.this.name
		
		def newPhase(_prev: Phase) = new SlamPhase(_prev)    
    
    class SlamPhase(prev: Phase) extends StdPhase(prev) {
			override def name = Slam.this.name

			def apply(unit: CompilationUnit) {
				(new ForeachTreeTraverser(handle)).traverse(unit.body)
			}
			
			def handle(tree: Tree): Unit = tree match {
				case Apply(fun, args) =>
					println("traversing application of "+ fun)
					
				case _ =>
					println(tree)
			}
		}
	}


  // def newPhase(prev: Phase): Phase = new TraverserPhase(prev)
	
  // class TraverserPhase(prev: Phase) extends StdPhase(prev) {
    // def apply(unit: CompilationUnit) {
      // newTraverser().traverse(unit.body)
    // }
  // }

  // def newTraverser(): Traverser = new ForeachTreeTraverser(check)

  // def check(tree: Tree): Unit = tree match {
    // case Apply(fun, args) =>
      // println("traversing application of "+ fun)
			
    // case _ =>
			// println(tree)
  // }
}