#version 330

layout (location=0) in vec3 position;

uniform mat4 modelMatrix;
// The cascade's light projection times the light's view matrix
uniform mat4 projViewMatrix;

void main()
{
    gl_Position = projViewMatrix * modelMatrix * vec4(position, 1.0);
}
