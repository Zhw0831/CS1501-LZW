/*************************************************************************
 *  Compilation:  javac LZWmod.java
 *  Execution:    java LZWmod - < input.txt   (compress)
 *  Execution:    java LZWmod + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *
 *************************************************************************/

public class LZWmod {
    private static final int R = 256;        // number of input chars
    private static int W = 9;         // minimum codeword width
    private static int L = 512;       // number of codewords = 2^W
    private static final int ini_W = 9; // the initial value of W
    private static final int ini_L = 512; // the initial value of L
    private static final int maxW = 16;       // maximum codeword width
    private static final int maxL = (int)Math.pow(2,maxW); // maximum number of codewords when W = maxW
    public static char reset; // the flag to indicate if the user wants to reset the dictionary

    public static void compress() {
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put(new StringBuilder("" + (char) i), i);
        int code = R+1;  // R is codeword for EOF

        // write in the reset flag to later check if the user wants to reset the dictionary
        BinaryStdOut.write(reset);

        //initialize the current string
        StringBuilder current = new StringBuilder();
        //read and append the first char
        char c = BinaryStdIn.readChar();
        current.append(c);
        Integer codeword = st.get(current);
        while (!BinaryStdIn.isEmpty()) {
            codeword = st.get(current);
            //read and append the next char to current
            char next = BinaryStdIn.readChar();
            current.append(next);
            if(!st.contains(current)){
              BinaryStdOut.write(codeword, W);

              if(code >=L && W<maxW){
                  // if the codebook is full, increase the codeword width and resize
                  W++;
                  L *= 2;
                  st.put(current, code++);
              }
              // the case that the user wants to reset the dictionary
              else if(code >= L && W==maxW && reset=='r'){
                  st = new TST<>();
                  // first put the ascii values and the chars in the ascii table to the dictionary
                  for (int i = 0; i < R; i++)
                      st.put(new StringBuilder("" + (char) i), i);
                  // change the W and L to their original values, set code back
                  code = R+1;
                  W = ini_W;
                  L = ini_L;
              }
              else if (code < L)
                    // Add to symbol table if not full
                    st.put(current, code++);
              //reset current
              current = new StringBuilder();
              current.append(next);
            }
        }

        //Write the codeword of whatever remains in current
        BinaryStdOut.write(st.get(current), W);

        BinaryStdOut.write(R, W); //Write EOF
        BinaryStdOut.close();
    }


    public static void expand() {
        // make sure the array size is large enough to accommodate the maximum possible number of code words
        String[] st = new String[maxL];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        // check if the dictionary was reset when compressing
        reset = BinaryStdIn.readChar();

        int codeword = BinaryStdIn.readInt(W);
        String val = st[codeword];

        while (true) {
            BinaryStdOut.write(val);

            // check for resize and codeword width increment
            // our next available codeword value has exceeded the current codebook size, so resize
            if(i>=L && W<maxW) {
                W++;
                L *= 2;
            }
            // check if there are any reset when compressing
            else if(i>=L && W==maxW && reset=='r'){
                st = new String[maxL];
                // reset
                // initialize symbol table with all 1-character strings
                for (i = 0; i < R; i++)
                    st[i] = "" + (char) i;
                st[i++] = "";                        // (unused) lookahead for EOF
                // change the W and L to their original values
                W = ini_W;
                L = ini_L;

                i = R;
            }

            codeword = BinaryStdIn.readInt(W);

            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) st[i++] = val + s.charAt(0);
            val = s;
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        // check that if the user wants to reset dictionary
        if(args.length > 1) {
            if(args[1].equals("r"))
                reset = 'r';
            else
                reset = 'n';
        }
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new RuntimeException("Illegal command line argument");
    }

}
