#version 330

in vec2 outTextureCoordinate;
out vec4 fragColor;

uniform vec4 diffuse;
uniform sampler2D textureSampler;
uniform int hasTexture;

void main()
{
    // The skybox is unlit: show the texture (or flat diffuse color) as-is
    if (hasTexture == 1) {
        fragColor = texture(textureSampler, outTextureCoordinate);
    } else {
        fragColor = diffuse;
    }
}
