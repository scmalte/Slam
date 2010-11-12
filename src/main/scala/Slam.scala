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
	
	var scalafile: Option[String] = None
	var gvfile: Option[String] = None
	var pdffile: Option[String] = None
	
	override def processOptions(options: List[String], error: String => Unit) {
		for (option <- options) {
			// if (option.startsWith("src:"))
				// scalafile = Some(option.substring("src:".length))
			if (option.startsWith("gv:"))
				gvfile = Some(option.substring("gv:".length))
			// else if (option.startsWith("pdf:")) {
				// pdffile = Some(option.substring("pdf:".length))
			else
				error("Option not understood: " + option)
		}
	}
	
	override val optionsHelp: Option[String] = Some("""
			-P:Slam:src:<file>             Scala file to parse
			-P:Slam:gv:<file>              Intermediate Graphviz file
		""".dedent)
						// -P:Slam:pdf:<file>             Final pdf
	
	private object Component extends PluginComponent {
		val global: Slam.this.global.type = Slam.this.global
		val phaseName = "slam"
		
		val runsAfter = List[String]("parser")
		override val runsRightAfter = Some("parser")
		
		val ignores = Set("scala.ScalaObject", "scala.Product", "scala.AnyRef", "Positional", "ASTNode")
		
		def newPhase(_prev: Phase) = new SlamPhase(_prev)
    
		object Styles {
			val ft = "Corbel"
			val mft = ft + " Italic"
			val fts = 11
			val sfts = (fts * 0.5).round
		}
		
    class SlamPhase(prev: Phase) extends StdPhase(prev) {		
			override val name = Slam.this.name

			lazy val outfile = new FileWriter(gvfile.get)
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

						node [fontsize=%1$s fontname="%2$s" width=0 height=0 penwidth=1.5]

						edge [fontsize=%1$s fontname="%2$s" style=solid color=gray50
									arrowsize=0.5]
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
					
					/* Also returns synthetic modifiers, e.g. <interface>, which we
 					 * don't want to appear in the list:
					 *
					 *   val modsstr = SFlags.flagsToString(cl.mods.flags)
					 */
					
					val mods = Map(GFlags.FINAL -> "final", GFlags.ABSTRACT -> "%s",
							GFlags.SEALED -> "sealed", GFlags.CASE -> "case",
							GFlags.PROTECTED -> "protected", GFlags.PRIVATE -> "private")
					
					var modsstr = mods.map(f => if (cl.mods.hasFlag(f._1)) f._2)
														.collect{case s: String => s}.mkString(" ")
					
					if (cl.mods.hasFlag(GFlags.TRAIT))
						modsstr = modsstr.format("")
					else
						modsstr = modsstr.format("abstract")
					
					var (compstr, shape, color) = 
						if (cl.mods.hasFlag(GFlags.TRAIT))
							("trait", "component", "darkviolet")
            // else if (cl.mods.hasFlag(GFlags.MODULE)) "object"
            else
							("class", "rect", "black")
					
					if (cl.mods.hasFlag(GFlags.CASE))
						color = "forestgreen"
					
					val style =
						if (cl.mods.hasFlag(GFlags.CASE))
							"rounded"
						else
							"solid" /* Default style anyway */
					
					val font =
						if (cl.mods.hasFlag(GFlags.ABSTRACT)
								&& !cl.mods.hasFlag(GFlags.TRAIT))
							mft
						else
							ft
					
					nodes.append("""
						%1$s [label=<
							<font point-size="%2$s" face="%3$s">%4$s %5$s</font>
							<br/>%1$s
						> shape=%6$s style=%7$s color=%8$s]
						""".dedent.format(cl.name, sfts, font, modsstr, compstr, shape,
														  style, color))
					
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