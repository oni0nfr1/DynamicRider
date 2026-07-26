#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform vec2 ArcCenter;
uniform vec2 ArcStartDirection;
uniform vec2 ArcEndDirection;
uniform int ArcClockwise;
uniform int ArcMajor;
uniform int ArcFull;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

const float EPSILON = 0.00001;

float cross2d(vec2 a, vec2 b) {
    return a.x * b.y - a.y * b.x;
}

void main() {
    vec2 pixelDirection = (texCoord0 - ArcCenter) * vec2(textureSize(Sampler0, 0));
    float directionLength = length(pixelDirection);
    if (ArcFull == 0 && directionLength > EPSILON) {
        pixelDirection /= directionLength;

        vec2 startDirection = normalize(ArcStartDirection);
        vec2 endDirection = normalize(ArcEndDirection);
        float orientation = ArcClockwise != 0 ? 1.0 : -1.0;
        float afterStart = orientation * cross2d(startDirection, pixelDirection);
        float beforeEnd = orientation * cross2d(pixelDirection, endDirection);
        bool inside = ArcMajor != 0
            ? afterStart >= -EPSILON || beforeEnd >= -EPSILON
            : afterStart >= -EPSILON && beforeEnd >= -EPSILON;

        if (!inside) {
            discard;
        }
    }

    vec4 sampled = texture(Sampler0, texCoord0);
    if (sampled.a == 0.0) {
        discard;
    }
    fragColor = sampled * vertexColor * ColorModulator;
}
