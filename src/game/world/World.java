package game.world;
import static org.lwjgl.opengl.GL11.*;

import game.Entity;
import game.Player;
import game.render.Camera;
import game.system.Input;
import game.world.generator.ChunkGeneratorTerrain;

import java.io.File;
import java.util.ArrayList;

public class World {
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	public ArrayList<Chunk> allChunks = new ArrayList<Chunk>();
	public ArrayList<Chunk> activeChunks = new ArrayList<Chunk>();
	public ArrayList<Chunk> chunkQueue = new ArrayList<Chunk>();
	
	public Input input;
	public Player player;
	private Camera camera;
	public ChunkGeneratorTerrain chunkGeneratorTerrain;
	
	public World(Input input, Player player, Camera camera) {
		this.input = input;
		this.player = player;
		this.camera = camera;
		this.chunkGeneratorTerrain = new ChunkGeneratorTerrain(this);
	}
	
	public void update() {
		//Update
	    
	    player.update(input);
		
		for (Entity e : entities) {
			e.update();
		}
		
		int pcx = (int) (camera.x/32);
		int pcy = (int) (camera.y/32);
		
		int cv = 7;
		int cvr = cv*2+1;
		int cvc = cv+1;
		
		Chunk[] suitableChunksFound = new Chunk[cvr*cvr];
		for (Chunk c : allChunks) {
			if (c.x > pcx-cvc && c.y > pcy-cvc && c.x < pcx+cvc && c.y < pcy+cvc) suitableChunksFound[((c.x-pcx+cv)*cvr+(c.y-pcy+cv))] = c;
			else if (activeChunks.contains(c)) {
				activeChunks.remove(c);
			}//*/
		}
		
		for (int i=0; i<cvr*cvr; i++) {
			if (suitableChunksFound[i] != null) {
				if (!activeChunks.contains(suitableChunksFound[i])) {
					activeChunks.add(suitableChunksFound[i]);
				}
			} else {
				System.out.println("New chunk added");
				allChunks.add(new Chunk((int) (pcx+(Math.floor(i/cvr))-cv),pcy+(i%cvr)-cv, this));
				activeChunks.add(allChunks.get(allChunks.size()-1));
			}
		}
		
		chunkQueue.clear();
		
		for (Chunk c : activeChunks) {
			if (!c.hasVBO) {
				chunkQueue.add(c);
			} else {
				c.render();
			}
		}
		
		glEnable(GL_BLEND);
		
		glBegin(GL_QUADS);
		glColor4f(0.2f,0.45f,0.98f,0.5f);
		glVertex3f(camera.x-128,15.5f,camera.y-128);
		glVertex3f(camera.x-128,15.5f,camera.y+128);
		glVertex3f(camera.x+128,15.5f,camera.y+128);
		glVertex3f(camera.x+128,15.5f,camera.y-128);
		glEnd();
		
		glDisable(GL_BLEND);
	}
	
	public boolean placeFree(float i, float j, float k) {
		return placeFree((int) i, (int) j, (int) k);
	}
	
	public boolean placeFree(int i, int j, int k) {
		return blockAt(i,j,k) == 0;
	}
	
	public byte blockAt(int i, int j, int k) {
		int cxp = (int) Math.floor(i/32);
		int cyp = (int) Math.floor(j/32);
		for (Chunk c : activeChunks) {
			if (c.x == cxp && c.y == cyp) {
				return c.chunkData[Math.abs(i%32)][Math.abs(j%32)][k%64];
			}
		}
		Chunk nc = new Chunk(cxp,cyp, this);
		return nc.chunkData[Math.abs(i%32)][Math.abs(j%32)][k%64];
	}
	
	public void setBlockAt(int i, int j, int k, byte id) {
		int cxp = (int) Math.floor(i/32);
		int cyp = (int) Math.floor(j/32);
		
		Chunk ctr = null;
		
		for (Chunk c : activeChunks) {
			if (c.x == cxp && c.y == cyp) {
				c.chunkData[Math.abs(i%32)][Math.abs(j%32)][k%64] = id;
				//c.hasVBO = false;
				//c.loadVBO();
				c.saveDataToFile(new File("world/"+c.x+"_"+c.y));
				//c.clearVBO();
				//ctr = c;
				//allChunks.remove(c);
			}
		}
		
		if (ctr != null) {
			activeChunks.remove(ctr);
		}
	}
	
	public Integer getSeed() {
		return 3337;
	}
	
	public Chunk getChunkAt(int i, int j) {
		for (Chunk c : activeChunks) {
			if (c.x == i && c.y == j) {
				return c;
			}
		}
		for (Chunk c : allChunks) {
			if (c.x == i && c.y == j) {
				return c;
			}
		}
		Chunk c = new Chunk(i,j, this);
		allChunks.add(c);
		return c;
	}
}
