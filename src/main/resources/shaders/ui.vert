#version 330

layout (location=0) in vec2 position;
layout (location=1) in vec2 textureCoordinate;
layout (location=2) in vec4 color;

out vec2 fragmentTextureCoordinate;
out vec4 fragmentColor;

// Maps ImGui's screen coordinates (origin top-left, y down) to clip space
uniform vec2 scale;
uniform vec2 translate;

void main()
{
    fragmentTextureCoordinate = textureCoordinate;
    fragmentColor = color;
    gl_Position = vec4(position * scale + translate, 0.0, 1.0);
}
