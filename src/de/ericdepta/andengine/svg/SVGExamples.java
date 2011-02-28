package de.ericdepta.andengine.svg;
/**
 * @author Eric Depta
 * @since 01:14:00 - 28.02.2011
 */
import java.util.HashMap;

public class SVGExamples extends HashMap<Integer, String>{
	private static final long serialVersionUID = 1L;
	
	private final static String SVG_PATH = "svg/";
	
	public SVGExamples(){
		this.put(R.id.svg1, SVG_PATH + "world_quads.svg");
		this.put(R.id.svg2, SVG_PATH + "world_pacman.svg");
		this.put(R.id.svg3, SVG_PATH + "world_fun.svg");
	}

}
