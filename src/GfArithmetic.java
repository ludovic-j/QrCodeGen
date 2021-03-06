import java.util.Arrays;

import static java.lang.Math.max;

public class GfArithmetic {
    int[] gfLog;
    int[] gfExp;
    int prime;

    GfArithmetic()
    {
        this.prime = 0x11d;
        this.initTables(this.prime);
    }

    private void initTables(int prime)
    {
        this.gfLog = new int[256];
        this.gfExp = new int[512];
        int x = 1;
        for(int i = 0; i < 255; i++)
        {
            this.gfExp[i] = x;
            this.gfExp[255 + i] = x;
            this.gfLog[x] = i;
            x = this.gfMulNoLUT(x,2, this.prime);
        }
        this.gfExp[510] = 1;
        this.gfExp[511] = 2;
    }
    public int gfAdd(int x, int y){
        // Addition dans les espaces de Galois
        return x ^ y;
    }
    public int gfSub(int x, int y){
        // Soustraction dans les espaces de Galois
        return x ^ y;
    }
    public int gfMul(int x, int y)
    {
        // Multiplie x par y
        if(x == 0 || y == 0)
            return 0;
        return this.gfExp[this.gfLog[x] + this.gfLog[y]];
    }

    public int gfDiv(int x, int y) throws ArithmeticException
    {
        // Fait la division de x par y et renvoie le reste
        if( y == 0 )
            throw new ArithmeticException("Division by Zero !!!");

        return this.gfExp[(this.gfLog[x] + 255 - gfLog[y]) % 255];
    }

    public int gfInv(int x)
    {
        // Calcule l'inverse de x
        return this.gfExp[255 -this.gfLog[x]];
    }

    public int gfPow(int x, int n)
    {
        // renvoie x ^ n
       return this.gfExp[(this.gfLog[x] * n) % 255];
    }



    /*
    *
    *  POLYNOMES
    *
    */

    public int polyEval(int[] p, int x)
     {
         // Evalue le polynôme en x (Schema de Horner)
         int l = p.length - 1;
         int res = p[l];
         for(int i = l- 1; i >= 0; i --)
             res = this.gfMul(res, x) ^ p[i];
         return res;
     }

     public int[] polyAdd(int[] p, int[] q)
     {
         // Routine d'addition de deux polynomes
         int l = max(p.length, q.length);
         int[] res = new int[l];
         int i;

         for(i = 0; i < p.length; i++)
             res[i] ^= p[i];

         for(i = 0; i < q.length; i++)
             res[i] ^= q[i];

         return stripPoly(res);
     }


     public int[] polyScal(int[] p, int alpha)
     {
         // Routine de multiplication d'un poly par un scalaire (qui utilise une LUT)
         int[] res = new int[p.length];
         for(int i = 0; i < p.length; i++)
         {
             res[i] = this.gfMul(p[i],alpha);
         }
         return res;
     }


     public int[] polyMul(int[] p, int[] q)
     {
         // Multiplication de 2 polynômes
         int i, j;
         int[] res;
         if(p.length == 1)
             res = new int[q.length];
         else if(q.length == 1)
             res = new int[p.length];
         else
             res = new int[p.length + q.length - 1];

         for(i = 0; i < p.length; i++)
         {
             for(j = 0; j < q.length ; j++)
                 res[i + j] ^= this.gfMul(p[i], q[j]);
         }
         return res;
     }

    public int[] polyDiv(int[] p, int[] q){
        int [] res =p.clone();
        for(int i=0;i<(p.length - q.length +1);i++){
            int coef=res[i];
            if (coef !=0){
                for(int j=1;j<q.length;j++){
                    if (q[j] !=0){
                        res[i+j]^=gfMul(q[j],coef);
                    }
                }
            }
        }
        return Arrays.copyOfRange(res,p.length-q.length+1,res.length);
    }

     public int[] polyDeriv(int[] p)
     {
         // Dérive le polynôme p
         int[] res = new int[p.length - 1];
         for(int i = 1; i < p.length + 1; i++)
         {
             if(i % 2 == 0)
                 res[i - 2] = p[i - 1];

         }
         return this.stripPoly(res);
     }

    /*
    *
    *
    * FONCTIONS ANNEXES
    *
    *
     */


    public int[] stripPoly(int[] p )
    {
        // Enlève les zeros en fin de polynome
        int[] res;
        int i = p.length -1;
        int j;
        while(i >= 0 && p[i] == 0)i--;
        if(i == -1)
            return new int[]{0};
        res = new int[i + 1];
        for(j = 0; j < res.length;j++)
            res[j] = p[j];
        return res;

    }
    private int bitLength(int i){
        // Renvoie la taille en nombre de bits de l'entier i
        int l = 0;
        while ((i >> l) > 0){l ++;}
        return l;
    }
    private int gfDivNoLUT(int x, int y) throws ArithmeticException
    {
        if (x == 0) return 0;
        if (y == 0) throw new ArithmeticException("Division by Zero");
        int d1 = bitLength(x);
        int d2 = bitLength(y);
        int res = x;
        int i;
        while (d1 >= d2)
        {
            i = d1 - d2;
            res ^= y << i;
            if (res != 0)
                d1 = bitLength(res);
            else
                break;
        }
        return res;
    }
    private int gfMulOverflow(int x, int y)
    {
        int res = 0;
        int i = 0;
        while((y >> i) > 0)
        {
            if((y & (1 << i)) != 0 ) {
                res ^= x << i;
            }

            i++;
        }
        return res;
    }
    private int gfMulNoLUT(int x, int y, int prime)
    {
        // Véritable routine de multiplication, on prend le résultat modulo un entier générateur
        int res = gfMulOverflow(x,y);
        return gfDivNoLUT(res,prime);
    }
    public int[] polyCopy(int[] p)
    {
        int[] res = new int[p.length];
        for(int i = 0; i < p.length; i++)
        {
            res[i] = p[i];
        }
        return res;
    }
    public int[] polyShift(int[] p) throws ArithmeticException
    {
        int[] res = new int[p.length + 1];
        for(int i = 0; i < p.length; i++)
        {
            res[i + 1] = p[i];
        }
        return res;
    }
    public int[] polyReverse(int[] p)
    {
        int[] res;
        int i =p.length - 1;
        while(p[i] == 0)i--;
        res = new int[i + 1];
        for(i = 0; i< res.length;i++)
            res[res.length - 1 -i] = p[i];
        return res;
    }

    public int[] createErasurePoly(int[] deg)
    {
        int[] res = new int[]{1};
        int[] monome;
        for(int i = 0 ; i < deg.length; i++)
        {
            monome = new int[]{1, gfPow(2,deg[i])};
            res = polyMul(res, monome);
        }
        return res;
    }
    public int[] createGenPoly(int N)
    {
        int[] res = new int[]{1};
        int[] monome;
        for(int i = 0 ; i < N; i++)
        {
            monome = new int[]{1, gfPow(2,i)};
            res = polyMul(res, monome);
        }
        return res;
    }


    // A Supprimer !!!!
    public int[] polyAddReverse(int[] p, int [] q)
    {
        return p;
    }
}
