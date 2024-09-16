class Solution:

    def odd_poly(n):
        return int(str(n) + str(n)[-2::-1])

    def even_poly(n):
        return int(str(n) + str(n)[::-1])

    def nearestPalindromic(self, n: str) -> str:
        N = int(n)

        l1 = 1
        r1 = 1000000000
        while (r1 - l1 > 1):
            m = (l1 + r1) // 2
            M = odd_poly(m)

            if (M < N):
                l1 = m
            elif (N < M):
                r1 = m
            else:
                return N

        l2 = 0
        r2 = 1000000000
        while (r2 - l2 > 1):
            m = (l2 + r2) // 2
            M = even_poly(m)

            if (M < N):
                l2 = m
            elif (N < M):
                r2 = m
            else:
                return N

        l1 = odd_poly(l1)
        r1 = odd_poly(r1)
        l2 = even_poly(l2)
        r2 = even_poly(r2)

        ans = []
        if (1 <= l1 < 1e18):
            ans.append(l1)
        if (1 <= r1 < 1e18):
            ans.append(r1)
        if (1 <= l2 < 1e18):
            ans.append(l2)
        if (1 <= r2 < 1e18):
            ans.append(r2)

        M = 1
        for m in ans:
            if (abs(m - N) < (abs(M - N))):
                M = m
        return M
