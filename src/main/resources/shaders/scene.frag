#version 330

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;
const float SPECULAR_POWER = 10;
// Set to 1 to tint each cascade (red, green, blue, nearest first) and check where the splits fall
const int DEBUG_SHADOWS = 0;
// Must match SHADOW_MAP_CASCADE_COUNT in CascadeShadows.kt
const int NUM_CASCADES = 3;
// Pushes the comparison slightly toward the light so surfaces don't shadow themselves ("shadow acne")
const float BIAS = 0.0005;
// How much light reaches a shadowed fragment
const float SHADOW_FACTOR = 0.25;

in vec3 outPosition;
in vec3 outNormal;
in vec3 outTangent;
in vec3 outBitangent;
in vec2 outTextureCoordinate;
in vec4 outWorldPosition;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};
struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
    int hasNormalMap;
};
struct AmbientLight
{
    float factor;
    vec3 color;
};
struct PointLight
{
    vec3 position;
    vec3 color;
    float intensity;
    Attenuation att;
};
struct SpotLight
{
    PointLight pl;
    vec3 conedir;
    float cutoff;
};
struct DirLight
{
    vec3 color;
    vec3 direction;
    float intensity;
};
struct Fog
{
    int activeFog;
    vec3 color;
    float density;
};
struct CascadeShadow
{
    // World space to the cascade's shadow map clip space
    mat4 projViewMatrix;
    // View-space z where the cascade ends (negative); the last cascade's is never read
    float splitDistance;
};

uniform sampler2D textureSampler;
uniform sampler2D normalSampler;
uniform Material material;
uniform AmbientLight ambientLight;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirLight dirLight;
uniform Fog fog;
uniform CascadeShadow cascadeshadows[NUM_CASCADES];
// One layer per cascade. A texture array, since GLSL 330 can't index a sampler array with a per-fragment value.
uniform sampler2DArray shadowMap;

vec4 calcAmbient(AmbientLight ambientLight, vec4 ambient)
{
    return vec4(ambientLight.factor * ambientLight.color, 1) * ambient;
}

vec4 calcLightColor(vec4 diffuse, vec4 specular, vec3 lightColor, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColor = vec4(0, 0, 0, 1);
    vec4 specColor = vec4(0, 0, 0, 1);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = diffuse * vec4(lightColor, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir, normal));
    float specularFactor = max(dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, SPECULAR_POWER);
    specColor = specular * light_intensity * specularFactor * material.reflectance * vec4(lightColor, 1.0);

    return (diffuseColor + specColor);
}

vec4 calcPointLight(vec4 diffuse, vec4 specular, PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.position - position;
    vec3 to_light_dir = normalize(light_direction);
    vec4 light_color = calcLightColor(diffuse, specular, light.color, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return light_color / attenuationInv;
}

vec4 calcSpotLight(vec4 diffuse, vec4 specular, SpotLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.pl.position - position;
    vec3 to_light_dir = normalize(light_direction);
    vec3 from_light_dir = -to_light_dir;
    float spot_alfa = dot(from_light_dir, normalize(light.conedir));

    vec4 color = vec4(0, 0, 0, 0);

    if (spot_alfa > light.cutoff)
    {
        color = calcPointLight(diffuse, specular, light.pl, position, normal);
        color *= (1.0 - (1.0 - spot_alfa) / (1.0 - light.cutoff));
    }
    return color;
}

vec4 calcDirLight(vec4 diffuse, vec4 specular, DirLight light, vec3 position, vec3 normal)
{
    return calcLightColor(diffuse, specular, light.color, light.intensity, position, normalize(light.direction), normal);
}

vec4 calcFog(vec3 pos, vec4 color, Fog fog, vec3 ambientLight, DirLight dirLight) {
    vec3 fogColor = fog.color * (ambientLight + dirLight.color * dirLight.intensity);
    float distance = length(pos);
    float fogFactor = 1.0 / exp((distance * fog.density) * (distance * fog.density));
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    vec3 resultColor = mix(fogColor, color.xyz, fogFactor);
    return vec4(resultColor.xyz, color.w);
}

vec3 calcNormal(vec3 normal, vec3 tangent, vec3 bitangent, vec2 textCoords) {
    mat3 TBN = mat3(tangent, bitangent, normal);
    vec3 newNormal = texture(normalSampler, textCoords).rgb;
    newNormal = normalize(newNormal * 2.0 - 1.0);
    newNormal = normalize(TBN * newNormal);
    return newNormal;
}

// Returns SHADOW_FACTOR if something nearer the light covers shadowCoord in cascade idx's map, otherwise 1
float textureProj(vec4 shadowCoord, vec2 offset, int idx) {
    float shadow = 1.0;

    if (shadowCoord.z > -1.0 && shadowCoord.z < 1.0) {
        float dist = texture(shadowMap, vec3(shadowCoord.xy + offset, idx)).r;
        if (shadowCoord.w > 0 && dist < shadowCoord.z - BIAS) {
            shadow = SHADOW_FACTOR;
        }
    }
    return shadow;
}

float calcShadow(vec4 worldPosition, int idx) {
    vec4 shadowMapPosition = cascadeshadows[idx].projViewMatrix * worldPosition;
    // Clip space (-1 to 1) to texture coordinates and depth (0 to 1)
    vec4 shadowCoord = (shadowMapPosition / shadowMapPosition.w) * 0.5 + 0.5;
    return textureProj(shadowCoord, vec2(0, 0), idx);
}

void main()
{
    vec4 text_color = texture(textureSampler, outTextureCoordinate);
    vec4 ambient = calcAmbient(ambientLight, text_color + material.ambient);
    vec4 diffuse = text_color + material.diffuse;
    vec4 specular = text_color + material.specular;

    vec3 normal = outNormal;
    if (material.hasNormalMap > 0) {
        normal = calcNormal(outNormal, outTangent, outBitangent, outTextureCoordinate);
    }

    vec4 diffuseSpecularComp = calcDirLight(diffuse, specular, dirLight, outPosition, normal);

    // View space looks down -z, so a fragment past a cascade's (negative) split distance belongs to a later one
    int cascadeIndex = 0;
    for (int i = 0; i < NUM_CASCADES - 1; i++) {
        if (outPosition.z < cascadeshadows[i].splitDistance) {
            cascadeIndex = i + 1;
        }
    }
    float shadowFactor = calcShadow(outWorldPosition, cascadeIndex);

    for (int i = 0; i < MAX_POINT_LIGHTS; i++)
    {
        if (pointLights[i].intensity > 0)
        {
            diffuseSpecularComp += calcPointLight(diffuse, specular, pointLights[i], outPosition, normal);
        }
    }

    for (int i = 0; i < MAX_SPOT_LIGHTS; i++)
    {
        if (spotLights[i].pl.intensity > 0)
        {
            diffuseSpecularComp += calcSpotLight(diffuse, specular, spotLights[i], outPosition, normal);
        }
    }
    fragColor = ambient + diffuseSpecularComp;
    fragColor.rgb = fragColor.rgb * shadowFactor;

    if (fog.activeFog == 1) {
        fragColor = calcFog(outPosition, fragColor, fog, ambientLight.color * ambientLight.factor, dirLight);
    }

    if (DEBUG_SHADOWS == 1) {
        switch (cascadeIndex) {
            case 0:
                fragColor.rgb *= vec3(1.0, 0.25, 0.25);
                break;
            case 1:
                fragColor.rgb *= vec3(0.25, 1.0, 0.25);
                break;
            case 2:
                fragColor.rgb *= vec3(0.25, 0.25, 1.0);
                break;
            default:
                fragColor.rgb *= vec3(1.0, 1.0, 0.25);
                break;
        }
    }
}
