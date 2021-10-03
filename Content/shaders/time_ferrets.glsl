#define PROCESSING_COLOR_SHADER

#ifdef GL_ES
precision mediump float;
#endif

#define iTime time
#define iResolution resolution
#define mainImage main
#define fragCoord gl_FragCoord
#define fragColor gl_FragColor

uniform float time;
uniform vec2 resolution;
uniform float depth;
uniform float rate;
////
// END LXSTUDIO BOILERPLATE
////

#define tau 6.283185307
#define pi tau/2.
#define sharp 0
#define arms 2.

// void mainImage( out vec4 fragColor, in vec2 fragCoord )
void main(void)
{
    // Normalized pixel coordinates (from 0 to 1)
    float m = max(iResolution.x, iResolution.y);
    vec2 uv = 0.5 * (fragCoord.xy - (0.5 * iResolution.xy)) / m;
    // uv = uv * 2.0 - vec2(1.0);

    // rt is (r, θ) but both normalized [0,1]
    vec2 rt = vec2(length(uv),atan(uv.y, uv.x)/tau + 0.5);
    float a = depth * cos(iTime * rate * rate);
    float b = iTime/3.;
    float c = sin(iTime);
    float d = mod((2.0 * rt.y) + rt.x * a + c, 1.0);
#if sharp
    if (d < 0.5) {
        d = 0.;
    } else {
        d = 1.;
    }
#endif
    // fragColor = vec4(
    //   uv,
    //   0.,
    //   0.
    // );
    fragColor = vec4( vec3(d), 1. );
    // fragColor = vec4(
    //     0.5 + 0.5 * sin(d * pi) ,
    //     0.,
    //     0.5 + 0.5 * sin((-d) * pi),
    //     1.
    // );
}
