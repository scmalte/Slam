package de.oakgrove.slam

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.PluginComponent

class Slam(val global: Global) extends PluginComponent {
  import global.{CompilationUnit, Traverser, ForeachTreeTraverser, Tree, Apply}
  // import global.definitions._

  val runsAfter = List[String]("parser")
  val phaseName = "slam"

  def newPhase(prev: Phase): Phase = new TraverserPhase(prev)
	
  class TraverserPhase(prev: Phase) extends StdPhase(prev) {
    def apply(unit: CompilationUnit) {
      // newTraverser().traverse(unit.body)
    }
  }

  def newTraverser(): Traverser = new ForeachTreeTraverser(check)

  def check(tree: Tree): Unit = () /* tree match {
    // case Apply(fun, args) =>
      // println("traversing application of "+ fun)
			
    case _ =>
			println("...")
  } */
}