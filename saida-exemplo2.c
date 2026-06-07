#include <stdio.h>
int main() {
    int a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;
    char str[512];
    {
        gets(str);
        sscanf(str, "%d", &n);
    }
    i = 2;
    a = n % i;
    while ( i < n ) {
        if ( a == 0 ) {
            i = n;
        }
        i = i + 1;
        a = n % i;
    }
    if ( a == 0 ) {
        printf("%d\n", 0);
    }
    if ( a != 0 ) {
        printf("%d\n", 1);
    }
    return 0;
}
