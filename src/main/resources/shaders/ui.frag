#version 330

in vec2 fragmentTextureCoordinate;
in vec4 fragmentColor;
out vec4 outColor;

uniform sampler2D textureSampler;

void main()
{
    outColor = fragmentColor * texture(textureSampler, fragmentTextureCoordinate);
}
