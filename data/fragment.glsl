#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define M_PI 3.1415926535897932384626433832795

uniform vec2 u_g_off;
uniform float u_g_scale;
uniform vec2 u_off;
uniform float u_scale;
uniform float u_hoff;
uniform float u_args[512];


vec3 precol[64];
uniform int u_aa;
int iterX = 0;
int iterY = 0;

// VALUES

vec3 g_x() {
	float scale = (u_g_scale * u_scale);
	float off = (u_g_off.x + u_off.x) * u_scale;
	float temp = off + gl_FragCoord.x * scale;
	temp = temp + (float(iterX)/float(u_aa)) * scale;
	return vec3(temp,temp,temp);
}

vec3 g_y() {
	float scale = u_g_scale * u_scale;
	float off = (u_g_off.y + u_off.y) * u_scale;
	float temp = off + (gl_FragCoord.y) * scale;
	temp = temp + (float(iterY)/float(u_aa)) * scale;
	return vec3(temp,temp,temp);
}

vec3 g_arg(int n) {
	return vec3(u_args[n*3], u_args[n*3+1], u_args[n*3+2]);
}

// BASIC MATH

vec3 g_add(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = a.x + b.x;
	temp.y = a.y + b.y;
	temp.z = a.z + b.z;
	return temp;
}

vec3 g_sub(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = a.x - b.x;
	temp.y = a.y - b.y;
	temp.z = a.z - b.z;
	return temp;
}

vec3 g_mult(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = a.x * b.x;
	temp.y = a.y * b.y;
	temp.z = a.z * b.z;
	return temp;
}

vec3 g_div(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = a.x / b.x;
	temp.y = a.y / b.y;
	temp.z = a.z / b.z;
	return temp;
}

// EXPONENTIAL

vec3 g_pow2(vec3 a) {
	vec3 temp;
	temp.x = pow(a.x,2);
	temp.y = pow(a.y,2);
	temp.z = pow(a.z,2);
	return temp;
}

vec3 g_sqrt(vec3 a) {
	vec3 temp;
	temp.x = sqrt(abs(a.x));
	temp.y = sqrt(abs(a.y));
	temp.z = sqrt(abs(a.z));
	return temp;
}

vec3 g_powOf(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = pow(a.x, abs(b.x));
	temp.y = pow(a.y, abs(b.y));
	temp.z = pow(a.z, abs(b.z));
	return temp;
}

vec3 g_logOf(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = log(a.x)/log(b.x);
	temp.y = log(a.y)/log(b.y);
	temp.z = log(a.z)/log(b.z);
	return temp;
}

vec3 g_2pow(vec3 a) {
	vec3 temp;
	temp.x = pow(2.0, a.x);
	temp.y = pow(2.0, a.y);
	temp.z = pow(2.0, a.z);
	return temp;
}

vec3 g_2log(vec3 a) {
	vec3 temp;
	temp.x = log(2.0)/log(a.x);
	temp.y = log(2.0)/log(a.y);
	temp.z = log(2.0)/log(a.z);
	return temp;
}

// ROUND

vec3 g_mod(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = mod(a.x,b.x);
	temp.y = mod(a.y,b.y);
	temp.z = mod(a.z,b.z);
	return temp;
}

vec3 g_fract(vec3 a) {
	vec3 temp;
	temp.x = fract(a.x);
	temp.y = fract(a.y);
	temp.z = fract(a.z);
	return temp;
}

vec3 g_floor(vec3 a) {
	vec3 temp;
	temp.x = floor(a.x);
	temp.y = floor(a.y);
	temp.z = floor(a.z);
	return temp;
}

vec3 g_ceil(vec3 a) {
	vec3 temp;
	temp.x = ceil(a.x);
	temp.y = ceil(a.y);
	temp.z = ceil(a.z);
	return temp;
}

vec3 g_round(vec3 a) {
	vec3 temp;
	temp.x = floor(a.x+0.5);
	temp.y = floor(a.y+0.5);
	temp.z = floor(a.z+0.5);
	return temp;
}

// TRIG

vec3 g_sin(vec3 a) {
	vec3 temp;
	temp.x = sin(a.x*M_PI)/2+0.5;
	temp.y = sin(a.y*M_PI)/2+0.5;
	temp.z = sin(a.z*M_PI)/2+0.5;
	return temp;
}

vec3 g_cos(vec3 a) {
	vec3 temp;
	temp.x = cos(a.x*M_PI)/2+0.5;
	temp.y = cos(a.y*M_PI)/2+0.5;
	temp.z = cos(a.z*M_PI)/2+0.5;
	return temp;
}

vec3 g_tan(vec3 a) {
	vec3 temp;
	temp.x = tan(a.x*M_PI);
	temp.y = tan(a.y*M_PI);
	temp.z = tan(a.z*M_PI);
	return temp;
}

vec3 g_asin(vec3 a) {
	vec3 temp;
	temp.x = asin(clamp(a.x,-1,1))/M_PI+0.5;
	temp.y = asin(clamp(a.y,-1,1))/M_PI+0.5;
	temp.z = asin(clamp(a.z,-1,1))/M_PI+0.5;
	return temp;
}

vec3 g_acos(vec3 a) {
	vec3 temp;
	temp.x = acos(clamp(a.x,-1,1))/M_PI;
	temp.y = acos(clamp(a.y,-1,1))/M_PI;
	temp.z = acos(clamp(a.z,-1,1))/M_PI;
	return temp;
}

vec3 g_atan(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = atan(clamp(a.x,-1,1), clamp(b.x,-1,1))/M_PI;
	temp.y = atan(clamp(a.y,-1,1), clamp(b.y,-1,1))/M_PI;
	temp.z = atan(clamp(a.z,-1,1), clamp(b.z,-1,1))/M_PI;
	return temp;
}

// CONSTRAIN

vec3 g_max(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = max(a.x,b.x);
	temp.y = max(a.y,b.y);
	temp.z = max(a.z,b.z);
	return temp;
}

vec3 g_min(vec3 a, vec3 b) {
	vec3 temp;
	temp.x = min(a.x,b.x);
	temp.y = min(a.y,b.y);
	temp.z = min(a.z,b.z);
	return temp;
}

vec3 g_clamp(vec3 a, vec3 b, vec3 c) {
	vec3 temp;
	temp.x = clamp(a.x,b.x,c.x);
	temp.y = clamp(a.y,b.y,c.y);
	temp.z = clamp(a.z,b.z,c.z);
	return temp;
}

vec3 g_abs(vec3 a) {
	vec3 temp;
	temp.x = abs(a.x);
	temp.y = abs(a.y);
	temp.z = abs(a.z);
	return temp;
}

// MIX

vec3 g_mix(vec3 a, vec3 b, vec3 c) {
	vec3 temp;
	temp.x = mix(a.x,b.x,c.x);
	temp.y = mix(a.y,b.y,c.y);
	temp.z = mix(a.z,b.z,c.z);
	return temp;
}

// ELSE

vec3 g_rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 g_hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

// HMM

vec3 g_combine(vec3 a, vec3 b, vec3 c) {
	float a_ = (a.x + a.y + a.z)/3;
	float b_ = (b.x + b.y + b.z)/3;
	float c_ = (c.x + c.y + c.z)/3;
	return vec3(a_,b_,c_);
}

vec3 g_setH(vec3 a, vec3 b) {
	float b_ = (b.x + b.y + b.z)/3;
	a = g_rgb2hsv(a);
	a.x = b_;
	a = g_hsv2rgb(a);
	return a;
}

vec3 g_offsetH(vec3 a, vec3 b) {
	float b_ = (b.x + b.y + b.z)/3;
	a = g_rgb2hsv(a);
	a.x += b_;
	a = g_hsv2rgb(a);
	return a;
}

vec3 g_setS(vec3 a, vec3 b) {
	float b_ = (b.x + b.y + b.z)/3;
	a = g_rgb2hsv(a);
	a.y = b_;
	a = g_hsv2rgb(a);
	return a;
}
vec3 g_setV(vec3 a, vec3 b) {
	float b_ = (b.x + b.y + b.z)/3;
	a = g_rgb2hsv(a);
	a.z = b_;
	a = g_hsv2rgb(a);
	return a;
}

// PROCESSING

vec3 process(vec3 c) {
	return g_offsetH(c, vec3(u_hoff,u_hoff,u_hoff));
}

void main() {
	int iter = 0;
	vec3 col;
	for (int y_ = 0; y_ < u_aa; y_++) {
		iterY = y_;
		for (int x_ = 0; x_ < u_aa; x_ ++) {
			iterX = x_;
			col = vec3(0.0,0.0,0.0);
			precol[iter] = col;

			iter ++;
		}
	}
	col = vec3(0.0,0.0,0.0);
	for (int i = 0; i < u_aa*u_aa; i++) {
		col = col + precol[i];
	}
	col = col / (u_aa * u_aa);
	col = process(col);
	gl_FragColor = vec4(col,1.0);
}