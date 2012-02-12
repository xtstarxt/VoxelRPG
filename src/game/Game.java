package game;

/* Code stolen from Nehe's Lesson 07 LWJGL version.
 * It's original header is below.
 *
 *      This Code Was Created By Jeff Molofee 2000
 *      A HUGE Thanks To Fredric Echols For Cleaning Up
 *      And Optimizing The Base Code, Making It More Flexible!
 *      If You've Found This Code Useful, Please Let Me Know.
 *      Visit My Site At nehe.gamedev.net
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.input.Keyboard;

/**
 * @author Mark Bernard
 * date:    16-Nov-2003
 *
 * Port of NeHe's Lesson 7 to LWJGL
 * Title: Texture Filters, Lighting & Keyboard Control
 * Uses version 0.8alpha of LWJGL http://www.lwjgl.org/
 *
 * Be sure that the LWJGL libraries are in your classpath
 *
 * Ported directly from the C++ version
 *
 * 2004-05-08: Updated to version 0.9alpha of LWJGL.
 *             Changed from all static to all instance objects.
 * 2004-09-22: Updated to version 0.92alpha of LWJGL.
 * 2004-12-19: Updated to version 0.94alpha of LWJGL and to use
 *             DevIL for image loading.
 */
public class Game {
	private boolean done = false;
	private boolean fullscreen = false;
	private final String windowTitle = "VoxelRPG";
	private boolean f1 = false;
	private DisplayMode displayMode;

	private Input input;
	private World world;
	private Player player;
	private Camera camera;

	public static void main(String args[]) {
		boolean fullscreen = false;
		if(args.length>0) {
			if(args[0].equalsIgnoreCase("fullscreen")) {
				fullscreen = true;
			}
		}

		Game game = new Game();
		game.run(fullscreen);
	}
	
	public void run(boolean fullscreen) {
		this.fullscreen = fullscreen;
		try {
			init();
			while (!done) {
				checkInput();
				render();
				Display.update();
			}
			cleanup();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void checkInput() {
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {       // Exit if Escape is pressed
			done = true;
		}
		if(Display.isCloseRequested()) {                     // Exit if window is closed
			done = true;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_F1) && !f1) {    // Is F1 Being Pressed?
			f1 = true;                                      // Tell Program F1 Is Being Held
			switchMode();                                   // Toggle Fullscreen / Windowed Mode
		}
		if(!Keyboard.isKeyDown(Keyboard.KEY_F1)) {          // Is F1 Being Pressed?
			f1 = false;
		}
	}

	private void switchMode() {
		fullscreen = !fullscreen;
		try {
			Display.setFullscreen(fullscreen);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private boolean render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);          // Clear The Screen And The Depth Buffer

		GL11.glLoadIdentity();                          // Reset The Current Modelview Matrix
		
		input.updateAxis();
		camera.updateView();
		
		world.update();
		
		return true;
	}
	private void createWindow() throws Exception {
		Display.setFullscreen(fullscreen);
		DisplayMode d[] = Display.getAvailableDisplayModes();
		for (int i = 0; i < d.length; i++) {
			if (d[i].getWidth() == 1280
					&& d[i].getHeight() == 960
					&& d[i].getBitsPerPixel() == 16) {
				displayMode = d[i];
				break;
			}
		}
		Display.setDisplayMode(displayMode);
		Display.setTitle(windowTitle);
		Display.create();
	}
	private void init() throws Exception {
		createWindow();

		initGL();
		
		input = new Input();
		player = new Player();
		world = new World(input,player);
		player.setWorld(world);
		camera = new Camera(input,player);
	}
	private void initGL() {
		GL11.glEnable(GL11.GL_TEXTURE_2D); // Enable Texture Mapping
		GL11.glShadeModel(GL11.GL_SMOOTH); // Enable Smooth Shading
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
		GL11.glClearDepth(1.0f); // Depth Buffer Setup
		GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
		GL11.glDepthFunc(GL11.GL_LEQUAL); // The Type Of Depth Testing To Do

		GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
		GL11.glLoadIdentity(); // Reset The Projection Matrix

		// Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(45.0f, (float) displayMode.getWidth() / (float) displayMode.getWidth(),0.1f,100.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW); // Select The Modelview Matrix

		// Really Nice Perspective Calculations
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		
		
		//Check to see if required extensions are supported
		if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
			System.out.println("GL VBOs are supported! :D");
		} else {
			System.out.println("GL VBOs aren't supported! D:");
			System.exit(1);
		}
		
	}
	private void cleanup() {
		Display.destroy();
	}
}
