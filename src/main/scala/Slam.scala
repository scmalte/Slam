package de.oakgrove.slam

import java.io.FileWriter
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
// import com.weiglewilczek.slf4s.Logging

class Slam(val global: Global) extends Plugin { // with Logging {
  import global.{CompilationUnit, Traverser, ForeachTreeTraverser, Tree}
	import global.{Apply, ClassDef}
	
	val name = "Slam"
	val description = "Creates class diagrams"
	val components = List[PluginComponent](Component)
	
	private object Component extends PluginComponent {
		val global: Slam.this.global.type = Slam.this.global
		val phaseName = "slam"
		
		val runsAfter = List[String]("parser")
		override val runsRightAfter = Some("parser")
		
		val ignores = Set("scala.ScalaObject", "scala.Product")
		
		def newPhase(_prev: Phase) = new SlamPhase(_prev)
    
    class SlamPhase(prev: Phase) extends StdPhase(prev) {		
			override val name = Slam.this.name

			lazy val outfile = new FileWriter("output.gv")
			
			override def run {
				writeHeader()
				super.run
				writeFooter()
				outfile.close
			}
			
			def apply(unit: CompilationUnit) {
				(new ForeachTreeTraverser(handleTree)).traverse(unit.body)
			}

			def handleTree(tree: Tree): Unit = tree match {
				case ClassDef(mods, name, tparams, impl) =>
					// println("-- 1 --" + tree.productPrefix)
					println("\t name = " + name)
					// println(impl.productPrefix)
					// println("\t mods = " + mods)
					// println("\t tparams = " + tparams)
					// println("\t impl.self = " + impl.self)
					val lin = impl.parents.filterNot(t => ignores.contains(t.toString))
					
					println("\t impl.parents = " + lin)
					outfile.write(name.toString + "\n")
					if (lin.nonEmpty) outfile.write(lin.mkString(" -> ", " -> ", "\n"))
				
				case _ => () // println(tree.productPrefix)
			}
			
			def writeHeader() {
				outfile.write("""
					digraph "scala.collection" {
						bgcolor=transparent
						
						node [shape=point, style=invis]
						edge [style=solid, color=gray]

						node [shape=box, style="rounded, filled", fontname=tahoma, fontsize=10, fontcolor=white, color=none, fillcolor=cadetblue]
				""")
			}
			
			def writeFooter() {
				outfile.write("}")
			}
		}
	}
}