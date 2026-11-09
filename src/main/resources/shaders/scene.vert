#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;
layout (location=2) in vec3 tangent;
layout (location=3) in vec3 bitangent;
layout (location=4) in vec2 textureCoordinate;

out vec3 outPosition;
out vec3 outNormal;
out vec3 outTangent;
out vec3 outBitangent;
out vec2 outTextureCoordinate;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
// Inverse transpose of the model-view matrix's upper 3x3, so normals stay perpendicular under non-uniform scale
uniform mat3 normalMatrix;

void main()
{
    mat4 modelViewMatrix = viewMatrix * modelMatrix;
    vec4 modelViewPosition = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * modelViewPosition;
    outPosition = modelViewPosition.xyz;
    outNormal = normalize(normalMatrix * normal);
    outTangent = normalize(modelViewMatrix * vec4(tangent, 0)).xyz;
    outBitangent = normalize(modelViewMatrix * vec4(bitangent, 0)).xyz;
    outTextureCoordinate = textureCoordinate;
}
