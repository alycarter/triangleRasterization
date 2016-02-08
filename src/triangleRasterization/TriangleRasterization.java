package triangleRasterization;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

class Triangle{
	public Vector3f points[] = new Vector3f[3];
	public Vector3f normals[] = new Vector3f[3];
	public Vector2f texCoords[] = new Vector2f[3];
	
	
	public Triangle() {
	
	}
	
	public Triangle mul(Matrix4f model, Matrix4f rotation){
		Triangle t = new Triangle();
		for(int i =0 ; i < 3; i++){
			Vector4f temp = Matrix4f.transform(model, new Vector4f(points[i].x, points[i].y, points[i].z, 1), null);
			t.points[i] = new Vector3f(temp.x, temp.y, temp.z); 
			temp = Matrix4f.transform(rotation, new Vector4f(normals[i].x, normals[i].y, normals[i].z, 1), null);
			t.normals[i] = new Vector3f(temp.x, temp.y, temp.z);  
			t.texCoords[i] = new Vector2f(texCoords[i].x, texCoords[i].y);
		}
		return t;
	}
	
	public Vector3f lerpPoint(float u, float v){
		Vector3f w1 = Vector3f.sub(points[1], points[0], null);
		Vector3f w2 = Vector3f.sub(points[2], points[0], null);
		Vector3f surface = Vector3f.add(new Vector3f(0, 0, 0), points[0], null);
		surface = Vector3f.add(surface, (Vector3f)Vector3f.add(w1, new Vector3f(0, 0, 0), null).scale(v), null);
		surface = Vector3f.add(surface, (Vector3f)Vector3f.add(w2, new Vector3f(0, 0, 0), null).scale(u), null);
		return surface;
	}
	
	public Vector2f lerpTexCoord(float u, float v){
		Vector2f w1 = Vector2f.sub(texCoords[1], texCoords[0], null);
		Vector2f w2 = Vector2f.sub(texCoords[2], texCoords[0], null);
		Vector2f surface = Vector2f.add(new Vector2f(0, 0), texCoords[0], null);
		surface = Vector2f.add(surface, (Vector2f)Vector2f.add(w1, new Vector2f(0, 0), null).scale(v), null);
		surface = Vector2f.add(surface, (Vector2f)Vector2f.add(w2, new Vector2f(0, 0), null).scale(u), null);
		return surface;
	}
	
	public Vector3f lerpNormal(float u, float v){
		Vector3f w1 = Vector3f.sub(normals[1], normals[0], null);
		Vector3f w2 = Vector3f.sub(normals[2], normals[0], null);
		Vector3f surface = Vector3f.add(new Vector3f(0, 0, 0), normals[0], null);
		surface = Vector3f.add(surface, (Vector3f)Vector3f.add(w1, new Vector3f(0, 0, 0), null).scale(v), null);
		surface = Vector3f.add(surface, (Vector3f)Vector3f.add(w2, new Vector3f(0, 0, 0), null).scale(u), null);
		return surface.normalise(surface);
		//return normals[0];
	}
	
	public float getMaxX(){
		float max = points[0].x;
		for(int i = 1 ; i < 3; i++){
			if(points[i].x > max){
				max = points[i].x;
			}
		}
		return max;
	}
	
	public float getMinX(){
		float max = points[0].x;
		for(int i = 1 ; i < 3; i++){
			if(points[i].x < max){
				max = points[i].x;
			}
		}
		return max;
	}
	
	public float getMaxY(){
		float max = points[0].y;
		for(int i = 1 ; i < 3; i++){
			if(points[i].y > max){
				max = points[i].y;
			}
		}
		return max;
	}
	
	public float getMinY(){
		float max = points[0].y;
		for(int i = 1 ; i < 3; i++){
			if(points[i].y < max){
				max = points[i].y;
			}
		}
		return max;
	}
}

public class TriangleRasterization extends JPanel{

	private static final long serialVersionUID = 1L;

	private JFrame frame;
	
	public Triangle triangles[];
	public Triangle renderTriangles[];
	
	public char values[] = {' ','`','.',':', '*', '"', 'l', 'O', '5', '8', '@', '#','#'};
	
	public BufferedImage depth;
	
	public BufferedImage image;
	
	public BufferedImage texture;
	
	float delta = 0;
	
	int width = 1280;
	int height = 720;
	
	int frameWidth = 80;
	int frameHeight = 45;
	
	public static void main(String[] args) {
		TriangleRasterization raster = new TriangleRasterization();
		raster.start();
		raster.run();
	}
	
	public void run() {
		float theta = 0;
		while(true){
			long start = System.currentTimeMillis();
			theta += delta * 90.0f *(Math.PI/180.0f);
			Matrix4f m1 =new Matrix4f();
			m1.setIdentity();
			m1.m00 = (float)Math.cos(theta);
			m1.m02 = (float)Math.sin(theta);
			m1.m20 = -(float)Math.sin(theta);
			m1.m22 = (float)Math.cos(theta);
			Matrix4f m2 = new Matrix4f();
			m2.setIdentity();
			m2.m30 = frameWidth / 2;
			m2.m31 = 5;
			m2.m32 = 200;
			for(int i = 0; i < triangles.length; i++){
				renderTriangles[i] = triangles[i].mul(Matrix4f.mul(m2, m1, null), m1);
			}
			//renderTriangle = triangle.mul(m2);
			render(frame.getBufferStrategy().getDrawGraphics());
			frame.getBufferStrategy().show();
			long end = System.currentTimeMillis();
			delta = (float)(end - start) / 1000.0f;
		}
	}

	public void start(){
		frame = new JFrame("triangle");
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setVisible(true);
		setSize(width,height);
		frame.add(this);
		frame.createBufferStrategy(2);
		
		triangles = new Triangle[4];
		renderTriangles = new Triangle[4];
		
		triangles[0] = new Triangle();
		triangles[0].points[0] = new Vector3f(0, 0, 0);
		triangles[0].normals[0] = new Vector3f(0, 0, 1);
		triangles[0].points[1] = new Vector3f(25, 25, 25);
		triangles[0].normals[1] = new Vector3f(0, 0, 1);
		triangles[0].points[2] = new Vector3f(-25, 25, 25);
		triangles[0].normals[2] = new Vector3f(0, 0, 1);
		triangles[0].texCoords[0] = new Vector2f(0.5f, 0.0f);
		triangles[0].texCoords[1] = new Vector2f(0.0f, 1.0f);
		triangles[0].texCoords[2] = new Vector2f(1.0f, 1.0f);
		
		triangles[1] = new Triangle();
		triangles[1].points[0] = new Vector3f(0, 0, 0);
		triangles[1].normals[0] = new Vector3f(0, 0, -1);
		triangles[1].points[1] = new Vector3f(25, 25, -25);
		triangles[1].normals[1] = new Vector3f(0, 0, -1);
		triangles[1].points[2] = new Vector3f(-25, 25, -25);
		triangles[1].normals[2] = new Vector3f(0, 0, -1);
		triangles[1].texCoords[0] = new Vector2f(0.5f, 0.0f);
		triangles[1].texCoords[1] = new Vector2f(0.0f, 1.0f);
		triangles[1].texCoords[2] = new Vector2f(1.0f, 1.0f);
		
		triangles[2] = new Triangle();
		triangles[2].points[0] = new Vector3f(0, 0, 0);
		triangles[2].normals[0] = new Vector3f(1, 0, 0);
		triangles[2].points[1] = new Vector3f(25, 25, -25);
		triangles[2].normals[1] = new Vector3f(1, 0, 0);
		triangles[2].points[2] = new Vector3f(25, 25, 25);
		triangles[2].normals[2] = new Vector3f(1, 0, 0);
		triangles[2].texCoords[0] = new Vector2f(0.5f, 0.0f);
		triangles[2].texCoords[1] = new Vector2f(0.0f, 1.0f);
		triangles[2].texCoords[2] = new Vector2f(1.0f, 1.0f);
		
		triangles[3] = new Triangle();
		triangles[3].points[0] = new Vector3f(0, 0, 0);
		triangles[3].normals[0] = new Vector3f(-1, 0, 0);
		triangles[3].points[1] = new Vector3f(-25, 25, -25);
		triangles[3].normals[1] = new Vector3f(-1, 0, 0);
		triangles[3].points[2] = new Vector3f(-25, 25, 25);
		triangles[3].normals[2] = new Vector3f(-1, 0, 0);
		triangles[3].texCoords[0] = new Vector2f(0.5f, 0.0f);
		triangles[3].texCoords[1] = new Vector2f(0.0f, 1.0f);
		triangles[3].texCoords[2] = new Vector2f(1.0f, 1.0f);
		
		image = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
		depth = new BufferedImage(frameWidth, frameHeight, BufferedImage.TYPE_INT_ARGB);
		
		try {
			texture = ImageIO.read(new File("test.png"));
		} catch (IOException e) {e.printStackTrace();}
	}
	
	protected void render(Graphics panel) {
		Graphics2D g = (Graphics2D)image.getGraphics();
		Graphics2D gd = (Graphics2D)depth.getGraphics();
		gd.setColor(Color.WHITE);
		gd.fillRect(0, 0, frameWidth, frameHeight);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, frameWidth, frameHeight);
		panel.setColor(Color.BLACK);
		panel.fillRect(0, 0, width, height);
		for(int i = 0 ; i < renderTriangles.length; i++){			
			renderTriangle(renderTriangles[i]);
		}
		for(int x = 0; x < frameWidth; x++){
			for(int y = 0; y < frameHeight; y++){
				
				Color c = new Color(image.getRGB(x, y));
				float hsb[] = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
				char value = values[(int)(hsb[2]*values.length)];
				panel.setColor(new Color(Color.HSBtoRGB(hsb[0], hsb[1], 1.0f)));
				panel.drawString(value +"", x * 16, y * 16);
			}	
		}
		
		panel.drawImage(image, 0, 0,width, height, null);
		panel.setColor(Color.WHITE);
		panel.drawString("delta: " + delta + " fps: " + 1.0f/delta, 10, 50);
	}
	
	private int clamp(int value, int min, int max){
		if(value < min ){
			value = min;
		}else{
			if(value > max){
				value = max;
			}
		}
		return value;
	}
	
	private void renderTriangle(Triangle t){
		Vector3f w1 = Vector3f.sub(t.points[1], t.points[0], null);
		Vector3f w2 = Vector3f.sub(t.points[2], t.points[0], null);
		int maxX = clamp((int)t.getMaxX(), 0, frameWidth);
		int maxY = clamp((int)t.getMaxY(), 0, frameHeight);
		int minX = clamp((int)t.getMinX(), 0, frameWidth);
		int minY = clamp((int)t.getMinY(), 0, frameHeight);
		
		for(int x = minX; x <maxX; x++){
			for(int y = minY; y <maxY; y++){
				Vector3f point = Vector3f.sub(new Vector3f(x, y, 0), t.points[0], null);
				float u =Vector3f.cross(w1, point, null).z / Vector3f.cross(w1, w2, null).z;
				float v =Vector3f.cross(point, w2, null).z / Vector3f.cross(w1, w2, null).z;
				if(v >= 0 && u >= 0 && v + u <= 1){
					Vector3f surface = t.lerpPoint(u, v);
					if(surface.z > 0 && -depth.getRGB(x, y) < surface.z){
						Vector3f normal = t.lerpNormal(u, v);
						float value = Vector3f.dot(normal, new Vector3f(0.1f, 1.0f, 1.0f).normalise(null));
						Vector2f texCoord = t.lerpTexCoord(u, v);
						texCoord.x *= texture.getWidth()-1;
						texCoord.y *= texture.getHeight()-1;
						if(value < 0){
							value = 0;
						}
						Color col = new Color(texture.getRGB((int)texCoord.x, (int)texCoord.y));
						
						image.setRGB(x, y, new Color((int)(col.getRed() * value), (int)(col.getGreen() * value), (int)(col.getBlue() * value)).getRGB());
						depth.setRGB(x, y, (int)-surface.z);	
					}
				}
			}	
		}
	}

}
