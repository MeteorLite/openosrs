package net.runelite.rs.api;

import net.runelite.mapping.Import;

public interface RSModelData extends RSRenderable
{
	@Import("faceCount")
	int getTriangleFaceCount$api();

	@Import("indices1")
	int[] getTrianglePointsX$api();

	@Import("indices2")
	int[] getTrianglePointsY$api();

	@Import("indices3")
	int[] getTrianglePointsZ$api();

	@Import("verticesX")
	int[] getVertexX$api();

	@Import("verticesY")
	int[] getVertexY$api();

	@Import("verticesZ")
	int[] getVertexZ$api();

	@Import("texTriangleX")
	short[] getTexTriangleX$api();

	@Import("texTriangleY")
	short[] getTexTriangleY$api();

	@Import("texTriangleZ")
	short[] getTexTriangleZ$api();

	@Import("faceTextures")
	short[] getFaceTextures$api();

	@Import("textureCoords")
	byte[] getTextureCoords$api();

	@Import("textureRenderTypes")
	byte[] getTextureRenderTypes$api();

	@Import("verticesCount")
	int getVerticesCount$api();

	@Import("vertexNormals")
	RSVertexNormal[] getVertexNormals$api();

	@Import("vertexVertices")
	RSVertexNormal[] getVertexVertices$api();

	@Import("recolor")
	void recolor$api(short var1, short var2);

	@Import("toModel")
	RSModel toModel$api(int var1, int var2, int var3, int var4, int var5);

	@Import("ambient")
	short getAmbient$api();

	@Import("contrast")
	short getContrast$api();
}
