package parser2;

public class parseTable {
    private final String[][] actionTable = new String[125][37];
    private final int[][] gotoTable = new int[125][30];
    private final String[] rules = new String[58];
    private final String[] terminal = new String[37];
    private final String[] nonTerminal = new String[30];

    public parseTable() {
        terminal[0] = "main";
        terminal[1] = ",";
        terminal[2] = "num";
        terminal[3] = "text";
        terminal[4] = "V";
        terminal[5] = "begin";
        terminal[6] = "end";
        terminal[7] = ";";
        terminal[8] = "skip";
        terminal[9] = "halt";
        terminal[10] = "print";
        terminal[11] = "return";
        terminal[12] = "N";
        terminal[13] = "T";
        terminal[14] = "<";
        terminal[15] = "input";
        terminal[16] = "=";
        terminal[17] = "(";
        terminal[18] = ")";
        terminal[19] = "if";
        terminal[20] = "then";
        terminal[21] = "else";
        terminal[22] = "not";
        terminal[23] = "sqrt";
        terminal[24] = "or";
        terminal[25] = "and";
        terminal[26] = "eq";
        terminal[27] = "grt";
        terminal[28] = "add";
        terminal[29] = "sub";
        terminal[30] = "mul";
        terminal[31] = "div";
        terminal[32] = "F";
        terminal[33] = "void";
        terminal[34] = "{";
        terminal[35] = "}";
        terminal[36] = "$";

        nonTerminal[0] = "PROG";
        nonTerminal[1] = "GLOBVARS";
        nonTerminal[2] = "VTYP";
        nonTerminal[3] = "VNAME";
        nonTerminal[4] = "ALGO";
        nonTerminal[5] = "INSTRUC";
        nonTerminal[6] = "COMMAND";
        nonTerminal[7] = "ATOMIC";
        nonTerminal[8] = "CONST";
        nonTerminal[9] = "ASSIGN";
        nonTerminal[10] = "CALL";
        nonTerminal[11] = "BRANCH";
        nonTerminal[12] = "TERM";
        nonTerminal[13] = "OP";
        nonTerminal[14] = "ARG";
        nonTerminal[15] = "COND";
        nonTerminal[16] = "SIMPLE";
        nonTerminal[17] = "COMPOSIT";
        nonTerminal[18] = "UNOP";
        nonTerminal[19] = "BINOP";
        nonTerminal[20] = "FNAME";
        nonTerminal[21] = "FUNCTIONS";
        nonTerminal[22] = "DECL";
        nonTerminal[23] = "HEADER";
        nonTerminal[24] = "FTYP";
        nonTerminal[25] = "BODY";
        nonTerminal[26] = "PROLOG";
        nonTerminal[27] = "EPILOG";
        nonTerminal[28] = "LOCVARS";
        nonTerminal[29] = "SUBFUNCS";

        //Adding the rules
        rules[0] = "PROG -> main GLOBVARS ALGO FUNCTIONS"; 
        rules[1] = "GLOBVARS -> ''";
        rules[2] = "GLOBVARS -> VTYP VNAME , GLOBVARS";
        rules[3] = "VTYP -> num";
        rules[4] = "VTYP -> text";
        rules[5] = "VNAME -> TokenV";
        rules[6] = "ALGO -> begin INSTRUC end";
        rules[7] = "INSTRUC -> ''";
        rules[8] = "INSTRUC -> COMMAND ; INSTRUC";
        rules[9] = "COMMAND -> skip";
        rules[10] = "COMMAND -> halt";
        rules[11] = "COMMAND -> print ATOMIC";
        rules[12] = "COMMAND -> ASSIGN";
        rules[13] = "COMMAND -> CALL";
        rules[14] = "COMMAND -> BRANCH";
        rules[15] = "COMMAND -> return ATOMIC";
        rules[16] = "ATOMIC -> VNAME";
        rules[17] = "ATOMIC -> CONST";
        rules[18] = "CONST -> TokenN";
        rules[19] = "CONST -> TokenT";
        rules[20] = "ASSIGN -> VNAME < input";
        rules[21] = "ASSIGN -> VNAME = TERM";
        rules[22] = "CALL -> FNAME ( ATOMIC , ATOMIC , ATOMIC )";
        rules[23] = "BRANCH -> if COND then ALGO else ALGO";
        rules[24] = "TERM -> ATOMIC";
        rules[25] = "TERM -> CALL";
        rules[26] = "TERM -> OP";
        rules[27] = "OP -> UNOP ( ARG )";
        rules[28] = "OP -> BINOP ( ARG , ARG )";
        rules[29] = "ARG -> ATOMIC";
        rules[30] = "ARG -> OP";
        rules[31] = "COND -> SIMPLE";
        rules[32] = "COND -> COMPOSIT";
        rules[33] = "SIMPLE -> BINOP ( ATOMIC , ATOMIC )";
        rules[34] = "COMPOSIT -> BINOP ( SIMPLE , SIMPLE )";
        rules[35] = "COMPOSIT -> UNOP ( SIMPLE )";
        rules[36] = "UNOP -> not";
        rules[37] = "UNOP -> sqrt";
        rules[38] = "BINOP -> or";
        rules[39] = "BINOP -> and";
        rules[40] = "BINOP -> eq";
        rules[41] = "BINOP -> grt";
        rules[42] = "BINOP -> add";
        rules[43] = "BINOP -> sub";
        rules[44] = "BINOP -> mul";
        rules[45] = "BINOP -> div";
        rules[46] = "FNAME -> tokenF";
        rules[47] = "FUNCTIONS -> ''";
        rules[48] = "FUNCTIONS -> DECL FUNCTIONS";
        rules[49] = "DECL -> HEADER BODY";
        rules[50] = "HEADER -> FTYP FNAME ( VNAME , VNAME , VNAME )";
        rules[51] = "FTYP -> num";
        rules[52] = "FTYP -> void";
        rules[53] = "BODY -> PROLOG LOCVARS ALGO EPILOG SUBFUNCS end";
        rules[54] = "PROLOG -> {";
        rules[55] = "EPILOG -> }";
        rules[56] = "LOCVARS -> VTYP VNAME , VTYP VNAME , VTYP VNAME ,";
        rules[57] = "SUBFUNCS -> FUNCTIONS";


        //Creating the parse action table 
        for (int i = 0; i < 125; i++) {
            for (int j = 0; j < 37; j++) {
                actionTable[i][j] = "";
            }
        }

        actionTable[0][0] = "s1";

        actionTable[1][8] = "s29";
        actionTable[1][9] = "r5";
        actionTable[1][38] = "r16";
        actionTable[1][39] = "r17";
        actionTable[1][40] = "r18";
        actionTable[1][41] = "r19";
        actionTable[1][73] = "s82";
        actionTable[1][78] = "s90";
        actionTable[1][79] = "s91";
        actionTable[1][84] = "s98";
        actionTable[1][85] = "s99";
        actionTable[1][93] = "r29";
        actionTable[1][94] = "r30";
        actionTable[1][95] = "s107";
        actionTable[1][96] = "s108";
        actionTable[1][105] = "s114";
        actionTable[1][106] = "r27";
        actionTable[1][113] = "s119";
        actionTable[1][117] = "r33";
        actionTable[1][121] = "r28";
        actionTable[1][125] = "s126";

        actionTable[2][1] = "s4";
        actionTable[2][6] = "s14";
        actionTable[2][11] = "s14";
        actionTable[2][29] = "s4";
        actionTable[2][31] = "r49";
        actionTable[2][32] = "s4";
        actionTable[2][33] = "r54";
        actionTable[2][35] = "r6";
        actionTable[2][88] = "s14";
        actionTable[2][89] = "r55";
        actionTable[2][90] = "s4";
        actionTable[2][112] = "r53";
        actionTable[2][119] = "s4";

        actionTable[3][1] = "s5";
        actionTable[3][29] = "s5";
        actionTable[3][32] = "s5";
        actionTable[3][33] = "r54";
        actionTable[3][90] = "s5";
        actionTable[3][119] = "s5";

        actionTable[4][3] = "s9";
        actionTable[4][4] = "r3";
        actionTable[4][5] = "r4";
        actionTable[4][7] = "s9";
        actionTable[4][20] = "s9";
        actionTable[4][24] = "s9";
        actionTable[4][36] = "s9";
        actionTable[4][44] = "s9";
        actionTable[4][45] = "s9";
        actionTable[4][63] = "s9";
        actionTable[4][64] = "s9";
        actionTable[4][75] = "s9";
        actionTable[4][80] = "s9";
        actionTable[4][81] = "s9";
        actionTable[4][82] = "s9";
        actionTable[4][91] = "s9";
        actionTable[4][98] = "s9";
        actionTable[4][100] = "s9";
        actionTable[4][104] = "s9";
        actionTable[4][107] = "s9";
        actionTable[4][108] = "s9";
        actionTable[4][114] = "s9";
        actionTable[4][123] = "s9";

        actionTable[5][1] = "r1";
        actionTable[5][2] = "s7";
        actionTable[5][29] = "r1";
        actionTable[5][61] = "r2";
        actionTable[5][62] = "s7";
        actionTable[5][74] = "s7";
        actionTable[5][97] = "s7";
        actionTable[5][126] = "r56";

        actionTable[6][6] = "r47";
        actionTable[6][7] = "r7";
        actionTable[6][11] = "r47";
        actionTable[6][16] = "s35";
        actionTable[6][30] = "r48";
        actionTable[6][31] = "r49";
        actionTable[6][36] = "r7";
        actionTable[6][65] = "r8";
        actionTable[6][88] = "r47";
        actionTable[6][89] = "r55";
        actionTable[6][102] = "s112";
        actionTable[6][103] = "r57";
        actionTable[6][112] = "r53";

        actionTable[7][9] = "r5";
        actionTable[7][17] = "s36";
        actionTable[7][18] = "r9";
        actionTable[7][19] = "r10";
        actionTable[7][21] = "r12";
        actionTable[7][22] = "r13";
        actionTable[7][23] = "r14";
        actionTable[7][34] = "r6";
        actionTable[7][37] = "r11";
        actionTable[7][38] = "r16";
        actionTable[7][39] = "r17";
        actionTable[7][40] = "r18";
        actionTable[7][41] = "r19";
        actionTable[7][42] = "r15";
        actionTable[7][66] = "r20";
        actionTable[7][67] = "r21";
        actionTable[7][68] = "r24";
        actionTable[7][69] = "r25";
        actionTable[7][70] = "r26";
        actionTable[7][106] = "r27";
        actionTable[7][109] = "r23";
        actionTable[7][121] = "r28";
        actionTable[7][122] = "r22";

        actionTable[8][7] = "s18";
        actionTable[8][36] = "s18";

        actionTable[9][7] = "s19";
        actionTable[9][36] = "s19";

        actionTable[10][7] = "s20";
        actionTable[10][36] = "s20";

        actionTable[11][7] = "s24";
        actionTable[11][36] = "s24";

        actionTable[12][20] = "s40";
        actionTable[12][24] = "s40";
        actionTable[12][44] = "s40";
        actionTable[12][45] = "s40";
        actionTable[12][75] = "s40";
        actionTable[12][80] = "s40";
        actionTable[12][81] = "s40";
        actionTable[12][82] = "s40";
        actionTable[12][98] = "s40";
        actionTable[12][100] = "s40";
        actionTable[12][107] = "s40";
        actionTable[12][108] = "s40";

        actionTable[13][20] = "s41";
        actionTable[13][24] = "s41";
        actionTable[13][44] = "s41";
        actionTable[13][45] = "s41";
        actionTable[13][75] = "s41";
        actionTable[13][80] = "s41";
        actionTable[13][81] = "s41";
        actionTable[13][82] = "s41";
        actionTable[13][98] = "s41";
        actionTable[13][100] = "s41";
        actionTable[13][107] = "s41";
        actionTable[13][108] = "s41";

        actionTable[14][9] = "r5";
        actionTable[14][25] = "s43";

        actionTable[15][43] = "s66";

        actionTable[16][9] = "r5";
        actionTable[16][25] = "s45";

        actionTable[17][26] = "s45";
        actionTable[17][28] = "r46";
        actionTable[17][34] = "s64";
        actionTable[17][49] = "s75";
        actionTable[17][50] = "s76";
        actionTable[17][51] = "r38";
        actionTable[17][52] = "r39";
        actionTable[17][53] = "r40";
        actionTable[17][54] = "r41";
        actionTable[17][55] = "r42";
        actionTable[17][56] = "r43";
        actionTable[17][57] = "r44";
        actionTable[17][58] = "r45";
        actionTable[17][59] = "r36";
        actionTable[17][60] = "r37";
        actionTable[17][71] = "s880";
        actionTable[17][72] = "s81";
        actionTable[17][86] = "s100";

        actionTable[18][9] = "r5";
        actionTable[18][38] = "r16";
        actionTable[18][39] = "r17";
        actionTable[18][40] = "r18";
        actionTable[18][41] = "r19";
        actionTable[18][87] = "s101";
        actionTable[18][92] = "s106";
        actionTable[18][93] = "r29";
        actionTable[18][94] = "r30";
        actionTable[18][106] = "r27";
        actionTable[18][110] = "s117";
        actionTable[18][111] = "s118";
        actionTable[18][115] = "s121";
        actionTable[18][116] = "s122";
        actionTable[18][117] = "r33";
        actionTable[18][120] = "s124";
        actionTable[18][121] = "r28";

        actionTable[19][7] = "s27";
        actionTable[19][36] = "s27";

        actionTable[20][46] = "s74";
        actionTable[20][47] = "r31";
        actionTable[20][48] = "r32";
        actionTable[20][101] = "r35";
        actionTable[20][117] = "r33";
        actionTable[20][118] = "r34";
        
        actionTable[21][35] = "r6";
        actionTable[21][83] = "s97";

        actionTable[22][27] = "s59";
        actionTable[22][44] = "s59";
        actionTable[22][80] = "s59";
        actionTable[22][81] = "s59";
        actionTable[22][107] = "s59";

        actionTable[23][27] = "s60";
        actionTable[23][44] = "s60";
        actionTable[23][80] = "s60";
        actionTable[23][81] = "s60";
        actionTable[23][107] = "s60";

        actionTable[24][27] = "s51";
        actionTable[24][44] = "s51";
        actionTable[24][75] = "s51";
        actionTable[24][76] = "s51";
        actionTable[24][80] = "s51";
        actionTable[24][81] = "s51";
        actionTable[24][99] = "s51";
        actionTable[24][107] = "s51";

        actionTable[25][27] = "s52";
        actionTable[25][44] = "s52";
        actionTable[25][75] = "s52";
        actionTable[25][76] = "s52";
        actionTable[25][80] = "s52";
        actionTable[25][81] = "s52";
        actionTable[25][99] = "s52";
        actionTable[25][107] = "s52";

        actionTable[26][27] = "s53";
        actionTable[26][44] = "s53";
        actionTable[26][75] = "s53";
        actionTable[26][76] = "s53";
        actionTable[26][80] = "s53";
        actionTable[26][81] = "s53";
        actionTable[26][99] = "s53";
        actionTable[26][107] = "s53";

        actionTable[27][27] = "s54";
        actionTable[27][44] = "s54";
        actionTable[27][75] = "s54";
        actionTable[27][76] = "s54";
        actionTable[27][80] = "s54";
        actionTable[27][81] = "s54";
        actionTable[27][99] = "s54";
        actionTable[27][107] = "s54";

        actionTable[28][27] = "s55";
        actionTable[28][44] = "s55";
        actionTable[28][75] = "s55";
        actionTable[28][76] = "s55";
        actionTable[28][80] = "s55";
        actionTable[28][81] = "s55";
        actionTable[28][99] = "s55";
        actionTable[28][107] = "s55";

        actionTable[29][27] = "s56";
        actionTable[29][44] = "s56";
        actionTable[29][75] = "s56";
        actionTable[29][76] = "s56";
        actionTable[29][80] = "s56";
        actionTable[29][81] = "s56";
        actionTable[29][99] = "s56";
        actionTable[29][107] = "s56";

        actionTable[30][27] = "s57";
        actionTable[30][44] = "s57";
        actionTable[30][75] = "s57";
        actionTable[30][76] = "s57";
        actionTable[30][80] = "s57";
        actionTable[30][81] = "s57";
        actionTable[30][99] = "s57";
        actionTable[30][107] = "s57";

        actionTable[31][27] = "58";
        actionTable[31][44] = "58";
        actionTable[31][75] = "58";
        actionTable[31][76] = "58";
        actionTable[31][80] = "58";
        actionTable[31][81] = "58";
        actionTable[31][99] = "58";
        actionTable[31][107] = "58";
        

        actionTable[32][7] = "s28";
        actionTable[32][13] = "s28";
        actionTable[32][14] = "r51";
        actionTable[32][15] = "r52";
        actionTable[32][36] = "s28";
        actionTable[32][44] = "s28";

        actionTable[33][6] = "s15";
        actionTable[33][11] = "s15";
        actionTable[33][31] = "r49";
        actionTable[33][35] = "r6";
        actionTable[33][88] = "s15";
        actionTable[33][89] = "r55";
        actionTable[33][112] = "r53";

        actionTable[34][89] = "s33";
        actionTable[34][124] = "r50";

        actionTable[35][35] = "r6";
        actionTable[35][77] = "s89";

        actionTable[36][6] = "r47";
        actionTable[36][10] = "a";
        actionTable[36][11] = "r47";
        actionTable[36][30] = "r48";
        actionTable[36][31] = "r49";
        actionTable[36][35] = "r6";
        actionTable[36][88] = "r47";
        actionTable[36][89] = "r55";
        actionTable[36][112] = "r53";
        
        //Creating the goto table
        for (int i = 0; i < 125; i++) {
            for (int j = 0; j < 30; j++) {
                gotoTable[i][j] = -1;
            }
        }
        gotoTable[1][1] = 2;
        gotoTable[1][29] = 61;
         

        gotoTable[2][1] = 3;
        gotoTable[2][29] = 3;
        gotoTable[2][32] = 63;
        gotoTable[2][90] = 104;
        gotoTable[2][119] = 123;

        gotoTable[3][3] = 8;
        gotoTable[3][7] = 25;
        gotoTable[3][20] = 38;
        gotoTable[3][24] = 38;
        gotoTable[3][36] = 25;
        gotoTable[3][44] = 38;
        gotoTable[3][45] = 38;
        gotoTable[3][63] = 78;
        gotoTable[3][64] = 79;
        gotoTable[3][75] = 38;
        gotoTable[3][80] = 38;
        gotoTable[3][81] = 38;
        gotoTable[3][82] = 38;
        gotoTable[3][91] = 105;
        gotoTable[3][98] = 38;
        gotoTable[3][100] = 38;
        gotoTable[3][104] = 113;
        gotoTable[3][107] = 38;
        gotoTable[3][108] = 38;
        gotoTable[3][114] = 120;
        gotoTable[3][123] = 125;
        
        gotoTable[4][2] = 6;
        gotoTable[4][62] = 77;
        gotoTable[4][74] = 83;
        gotoTable[4][97] = 109;

        gotoTable[5][7] = 16;
        gotoTable[5][36] = 65;

        gotoTable[6][7] = 17;
        gotoTable[6][36] = 17;

        gotoTable[7][20] = 20;
        gotoTable[7][24] = 42;
        gotoTable[7][44] = 68;
        gotoTable[7][45] = 73;
        gotoTable[7][75] = 84;
        gotoTable[7][80] = 93;
        gotoTable[7][81] = 93;
        gotoTable[7][82] = 96; 
        gotoTable[7][98] = 110;
        gotoTable[7][100] = 84;
        gotoTable[7][107] = 93;
        gotoTable[7][108] = 116;

        gotoTable[8][20] = 39;
        gotoTable[8][24] = 39;
        gotoTable[8][44] = 39;
        gotoTable[8][45] = 39;
        gotoTable[8][75] = 39;
        gotoTable[8][80] = 39;
        gotoTable[8][81] = 39;
        gotoTable[8][82] = 39; 
        gotoTable[8][98] = 39;
        gotoTable[8][100] = 39;
        gotoTable[8][107] = 39;
        gotoTable[8][108] = 39;

        gotoTable[9][7] = 21;
        gotoTable[9][36] = 21;

        gotoTable[10][7] = 22;
        gotoTable[10][36] = 22;
        gotoTable[10][44] = 69;

        gotoTable[11][7] = 23;
        gotoTable[11][36] = 23;

        gotoTable[12][44] = 67;

        gotoTable[13][44] = 70;
        gotoTable[13][80] = 94;
        gotoTable[13][81] = 94;
        gotoTable[13][107] = 94;

        gotoTable[14][80] = 92;
        gotoTable[14][81] = 95;
        gotoTable[14][107] = 115;

        gotoTable[15][27] = 46;


        gotoTable[16][27] = 47;
        gotoTable[16][75] = 85;
        gotoTable[16][76] = 87;
        gotoTable[16][99] = 111;


        gotoTable[17][27] = 48;


        gotoTable[18][27] = 50;
        gotoTable[18][44] = 71;
        gotoTable[18][80] = 71;
        gotoTable[18][81] = 71;
        gotoTable[18][107] = 71;


        gotoTable[19][27] = 49;
        gotoTable[19][44] = 72;
        gotoTable[19][75] = 86;
        gotoTable[19][76] = 86;
        gotoTable[19][80] = 72;
        gotoTable[19][81] = 72;
        gotoTable[19][99] = 86;
        gotoTable[19][107] = 72;

        gotoTable[20][7] = 26;
        gotoTable[20][13] = 34;
        gotoTable[20][36] = 26;
        gotoTable[20][44] = 26;

        gotoTable[21][6] = 10;
        gotoTable[21][11] = 30;
        gotoTable[21][88] = 103;

        gotoTable[22][6] = 11;
        gotoTable[22][11] = 11;
        gotoTable[22][88] = 11;

        gotoTable[23][6] = 12;
        gotoTable[23][11] = 12;
        gotoTable[23][88] = 12;
        
        gotoTable[24][6] = 13;
        gotoTable[24][11] = 13;
        gotoTable[24][88] = 13;

        gotoTable[25][12] = 31;

        gotoTable[26][12] = 32;

        gotoTable[27][77] = 88;

        gotoTable[28][32] = 62;

        gotoTable[29][77] = 102;

    }

    public String getAction(int state, String terminal) {
        int index = -1;
        for (int i = 0; i < 37; i++) {
            if (this.terminal[i].equals(terminal)) {
                index = i;
                break;
            }
        }
        return actionTable[state][index];
    }

    // Get the next state from the goto table
    public int getGoto(int state, String nonTerminal) {
        int index = -1;
        for (int i = 0; i < 30; i++) {
            if (this.nonTerminal[i].equals(nonTerminal)) {
                index = i;
                break;
            }
        }
        return gotoTable[state][index];
    }
}
