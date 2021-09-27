uniform vec4 u_abcd;
uniform float u_unitLength;

varying vec4 v_worldPosition;

void main( void )
{
    float depth = dot(u_abcd.xyz, v_worldPosition.xyz) + u_abcd.w;
    vec3 color = vec3(depth / u_unitLength);
    gl_FragColor = vec4( color, 1.0 );
}
