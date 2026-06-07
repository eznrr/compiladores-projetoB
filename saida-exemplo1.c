#include <stdio.h>
int main() {
    int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;
    char str[512];
    {
        gets(str);
        sscanf(str, "%d", &n);
    }
    {
        gets(str);
        sscanf(str, "%d", &p);
    }
    i = 0;
    while ( i != n ) {
        a = p * i;
        printf("%d\n", a);
        i = i + 1;
    }
    return 0;
}
