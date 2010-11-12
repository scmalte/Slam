package de.oakgrove.slam

import java.io.FileWriter
import scala.collection.mutable.StringBuilder
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}
// import com.weiglewilczek.slf4s.Logging
import scala.reflect.generic._
import SlamUtils.StringToRichString
import scala.tools.nsc.symtab.{Flags => SFlags}
import scala.reflect.generic.{Flags => GFlags}

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
		
		val ignores = Set("scala.ScalaObject", "scala.Product", "scala.AnyRef")
		
		def newPhase(_prev: Phase) = new SlamPhase(_prev)
    
		object Styles {
			val ft = "Corbel"
			val mft = ft // + " Italic"
			val fts = 11
			val sfts = (fts * 0.5).round
		}
		
    class SlamPhase(prev: Phase) extends StdPhase(prev) {		
			override val name = Slam.this.name

			lazy val outfile = new FileWriter("output.gv")			
			val nodes = new StringBuffer()
			val edges = new StringBuffer()
			
			val ident = "\t"
			
			override def run {
				writeHeader()
				super.run
				outfile.write(nodes.toString)
				outfile.write(edges.toString)
				writeFooter()
				outfile.close
			}
			
			def apply(unit: CompilationUnit) {
				(new ForeachTreeTraverser(handleTree)).traverse(unit.body)
			}

			def handleTree(tree: Tree): Unit = tree match {
				case cl @ ClassDef(_, _, _, _) => writeClassDef(cl)
				case _ => () // println(tree.productPrefix)
			}
			
			def writeHeader() {
				import Styles._
				
				outfile.write("""
					digraph "Slam" {
						graph [bgcolor=transparent]

						node [fontsize=%1$s fontname="%2$s" shape=rect width=0 height=0
									style="rounded"] //invis

						edge [fontsize=%1$s fontname="%2$s" style=solid color=gray]
				""".dedent.format(fts, ft))
			}
			
			def writeFooter() {
				outfile.write("}")
			}
			
			def writeClassDef(cl: ClassDef) {
					import Styles._

					println(cl.name)
					
					val lin = cl.impl.parents.filterNot(t => ignores.contains(t.toString))
					
					println("\t parents = " + lin)
					// println("\t cl = " + cl)
					// println("\t cl.impl.self = " + cl.impl.self)
					
					val modsstr = "" // SFlags.flagsToString(cl.mods.flags)
					
					val compstr = 
						if (cl.mods.hasFlag(GFlags.TRAIT)) "trait"
            // else if (cl.mods.hasFlag(GFlags.MODULE)) "object"
            else "class"
					
					nodes.append("""
						%1$s [label=<
							<font point-size="%2$s" face="%3$s">%4$s %5$s</font>
							<br/>%1$s
						>]
						""".dedent.format(cl.name, sfts, mft, modsstr, compstr))
					
					// println("\t mods = " + cl.mods)
					
					edges.append(
						if (lin.nonEmpty) {
							lin map (ident + cl.name + " -> " + _ + "\n") mkString
						} else {
							ident + cl.name + "\n"
						})
			}
		}
	}
}