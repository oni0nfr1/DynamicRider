#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in float hueShift;
in float opacity;

out vec4 fragColor;

const float TAU = 6.28318530718;

void main() {
    vec4 sampled = texture(Sampler0, texCoord0);
    if (sampled.a == 0.0) {
        discard;
    }

    float y = dot(sampled.rgb, vec3(0.299, 0.587, 0.114));
    float i = dot(sampled.rgb, vec3(0.596, -0.274, -0.322));
    float q = dot(sampled.rgb, vec3(0.211, -0.523, 0.312));
    float angle = hueShift * TAU;
    float rotatedI = i * cos(angle) - q * sin(angle);
    float rotatedQ = i * sin(angle) + q * cos(angle);
    vec3 shifted = vec3(
        y + 0.956 * rotatedI + 0.621 * rotatedQ,
        y - 0.272 * rotatedI - 0.647 * rotatedQ,
        y - 1.106 * rotatedI + 1.703 * rotatedQ
    );

    fragColor = vec4(clamp(shifted, 0.0, 1.0), sampled.a * opacity) * ColorModulator;
}
