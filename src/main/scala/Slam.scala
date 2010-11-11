package de.oakgrove.slam

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
// import com.weiglewilczek.slf4s.Logging

class Slam(val global: Global) extends Plugin { // with Logging {
  import global.{CompilationUnit, Traverser, ForeachTreeTraverser, Tree}
	import global.{Apply, ClassDef}
	
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

/*
(sym: Slam.this.global.Symbol,constrMods: Slam.this.global.Modifiers,vparamss: List[List[Slam.this.global.ValDef]],argss:List[List[Slam.this.global.Tree]],body: List[Slam.this.global.Tree],superPos: Slam.this.global.Position)Slam.this.global.ClassDef

(sym: Slam.this.global.Symbol,impl: Slam.this.global.Template)Slam.this.global.ClassDef 

object Slam.this.global.ClassDef
*/

			def handle(tree: Tree): Unit = tree match {
				case cd @ ClassDef(mods, name, tparams, impl) => 
					println("-- 1 --" + tree.productPrefix)
					println("\t name = " + name)
					println(impl.productPrefix)
					// println("\t mods = " + mods)
					// println("\t tparams = " + tparams)
				
				case _ => () //println(tree.productPrefix)
			}
		}
	}
}