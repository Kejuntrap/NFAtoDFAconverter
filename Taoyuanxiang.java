import javax.lang.model.type.ArrayType;
import java.util.*;

public class Taoyuanxiang {     //convert from NFA to DFA ファッとして桃源郷
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        HashMap<String, State> beforeNFA = new HashMap<String, State>();         //状態の集合 Q に当たる 変換前のNFA
        HashMap<String, State> afterDFA = new HashMap<String, State>();         //状態の集合 Q に当たる 変換後のDFA
        ArrayList<String> alphabets = new ArrayList<String>();     //使う文字列の集合　\Sigma に当たる  空文字列はEPS
        ArrayList<String> storeStates = new ArrayList<String>();   // NFAで使われている状態を保存する
        ArrayList<ArrayList<String>> rekkyoStates = new ArrayList<ArrayList<String>>();

        int numAlphabet = sc.nextInt(); //アルファベットの種類
        for (int itr = 0; itr < numAlphabet; itr++) {
            String s = sc.next();
            alphabets.add(s);
        }
        int numState = sc.nextInt();      //状態数 タマの数
        int numTransition = sc.nextInt();      //遷移数 要するに矢印の数
        for (int itr = 0; itr < numState; itr++) {      //状態の情報を読み込む
            String stateName = sc.next();
            boolean isAccepted = sc.nextBoolean();
            boolean isStart = sc.nextBoolean();
            State addState = new State(stateName, 0, isAccepted, isStart);
            beforeNFA.put(stateName, addState);
        }
        for (int itr = 0; itr < numTransition; itr++) {     //遷移の情報を読み込む
            String transitionFrom = sc.next();
            String transitionChar = sc.next();
            String transitionTo = sc.next();
            if (transitionChar.equals("EPS")) {     //空文字列
                if (beforeNFA.get(transitionFrom).equals(beforeNFA.get(transitionTo))) {
                    //EPSの遷移で遷移前と遷移後で同じ状態を指していると無限ループになる
                } else {
                    (beforeNFA.get(transitionFrom)).addTransition(transitionChar, transitionTo); //遷移を追加
                }
            } else {
                (beforeNFA.get(transitionFrom)).addTransition(transitionChar, transitionTo); //遷移を追加
            }
        }
        for (String stateName : beforeNFA.keySet()) {       //状態を保存
            storeStates.add(stateName);
        }
        int itr = 1 << storeStates.size();      //状態の冪集合を列挙
        for (int i = 0; i < itr; i++) {
            if (i == 0) {
                ArrayList<String> po = new ArrayList<String>();
                po.add("∅");
                rekkyoStates.add(po);
            } else {
                ArrayList<Integer> popBits = calcBits(i);
                ArrayList<String> po = new ArrayList<String>();
                for (int p : popBits) {
                    po.add(storeStates.get(p));
                }
                Collections.sort(po);
                rekkyoStates.add(po);
            }
        }
        ArrayList<ArrayList<HashSet<String>>> transitionDisplay = new ArrayList<ArrayList<HashSet<String>>>();   //遷移表
        for (int i = 0; i < rekkyoStates.size(); i++) {
            if (i == 0) {   //EPS
                ArrayList<HashSet<String>> charsets = new ArrayList<HashSet<String>>();     //1つの状態の集合における受け取る文字による遷移先を保管する
                for (int j = 0; j < alphabets.size(); j++) {
                    if (!alphabets.get(j).equals("EPS")) {       //DFAでは空文字が使えない
                        HashSet<String> transitionTo = new HashSet<String>();       //遷移先の配列
                        transitionTo.add("∅");
                        charsets.add(transitionTo);     //文字ごとの遷移の集合を入れる
                    }
                }
                transitionDisplay.add(charsets);
            } else {  //空集合以外の要素をもつ状態の冪集合について見る
                ArrayList<HashSet<String>> charsets = new ArrayList<HashSet<String>>();     //1つの状態の集合における受け取る文字による遷移先を保管する
                for (int j = 0; j < alphabets.size(); j++) {
                    HashSet<String> transitionTo = new HashSet<String>();       //遷移先の配列   入っている要素はユニークだが、順序が保証されないのであとで適宜処理を行う必要がある
                    for (int k = 0; k < rekkyoStates.get(i).size(); k++) {        //複数の状態が入っているのでそれぞれの集合においてみてあげなければならない
                        if (!alphabets.get(j).equals("EPS")) {       //DFAでは空文字が使えない
                            if (!beforeNFA.get(rekkyoStates.get(i).get(k)).transition.containsKey(alphabets.get(j))) {  //beforeNFA.get( rekkyoStates.get(i).get(k) ) は状態配列のi番目の要素の集合のk番目の要素に当たるNFAの遷移
                                if (transitionTo.size() == 0) {
                                    transitionTo.add("∅");  //もとのNFAにおいてその状態からその文字を読み込んだときの遷移先がない場合 かつ 他の状態によって行ける場所が本当にない場合にのみ∅となる
                                } else {
                                    //ほかの状態が遷移で他の状態に行けることが確定しているならば、遷移先は∅ではない。
                                }
                            } else {     //遷移先があるとき
                                // NFAに.get("今いる状態の名前").transition.get("入力の文字") -> その入力によって遷移する状態の名前が列挙されて帰ってきます
                                ArrayList<String> tmpt = beforeNFA.get(rekkyoStates.get(i).get(k)).transition.get(alphabets.get(j));
                                int findEPS = 0;

                                while (tmpt.size() > findEPS) {   //EPSで行ける所の探索   ダブリがある可能性があるので、これを格納するHashSetによってそれを吸収させる
                                    String points = tmpt.get(findEPS);
                                    if (beforeNFA.get(points).transition.containsKey("EPS")) {
                                        ArrayList<String> t = beforeNFA.get(points).transition.get("EPS");  //EPSがあり、行ける先の列挙したもの
                                        for (String tt : t) {
                                            tmpt.add(tt);       //NFAには∅に遷移はしないので、追加される遷移先の状態は必ず何らかの実体を持つ
                                        }
                                    }
                                    findEPS++;
                                }
                                if (tmpt.size() > 0) {      //遷移先があるとき
                                    if (transitionTo.size() == 1 && transitionTo.contains("∅")) {
                                        transitionTo.remove("∅");   //遷移先があるので遷移先は∅ではない
                                    }
                                    for (String addt : tmpt) {
                                        transitionTo.add(addt); //遷移先を入れる
                                    }
                                } else {
                                    if (transitionTo.size() == 0) {
                                        transitionTo.add("∅");
                                    }
                                }
                            }
                        } else {
                            //EPSはDFAにない
                        }
                    }
                    if (!alphabets.get(j).equals("EPS")) {
                        charsets.add(transitionTo); //文字ごとの遷移の集合を入れる
                    }
                }
                transitionDisplay.add(charsets);
            }
        }
        cvtNFAtDFASht(transitionDisplay, alphabets, rekkyoStates, beforeNFA);
        lp("");
        cvtNFAtDFA(transitionDisplay, alphabets, rekkyoStates, beforeNFA);
    }

    static void cvtNFAtDFA(ArrayList<ArrayList<HashSet<String>>> tmp, ArrayList<String> alphabets, ArrayList<ArrayList<String>> rekkyo, HashMap<String, State> beforeNFA) {// for graphiz
        ArrayList<String> starts = new ArrayList<String>();
        ArrayList<String> goals = new ArrayList<String>();
        int startT = 0; //最初のスタート
        for (String stateName : beforeNFA.keySet()) {
            if (beforeNFA.get(stateName).isStart) {
                starts.add(stateName);
            }
            if (beforeNFA.get(stateName).isAccepted) {
                goals.add(stateName);
            }
        }
        while (startT < starts.size()) {
            String t = starts.get(startT);
            if (beforeNFA.get(t).transition.containsKey("EPS")) {
                ArrayList<String> epsS = beforeNFA.get(t).transition.get("EPS");
                for (String ss : epsS) {
                    starts.add(ss);
                }
            }
            startT++;
        }
        Collections.sort(starts);

        ArrayList<ArrayList<ArrayList<String>>> automa = new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<String> usechar = new ArrayList<String>();
        for (String s : alphabets) {
            if (!s.equals("EPS")) {
                usechar.add(s);
            }
        }
        for (int i = 0; i < tmp.size(); i++) {
            ArrayList<ArrayList<String>> tmptrans = new ArrayList<ArrayList<String>>();
            for (int j = 0; j < usechar.size(); j++) {
                ArrayList<String> t = new ArrayList<String>();
                for (String ss : tmp.get(i).get(j)) {
                    t.add(ss);
                }
                Collections.sort(t);
                tmptrans.add(t);
            }
            automa.add(tmptrans);
        }
        lp("digraph G {");
        lp("\tempty [label = \"\" shape = plaintext];");
        lp("");
        for (int i = 0; i < rekkyo.size(); i++) {
            boolean goalflag = false;
            for (int j = 0; j < rekkyo.get(i).size(); j++) {
                for (String ss : goals) {
                    if (rekkyo.get(i).get(j).equals(ss)) {
                        goalflag = true;
                    }
                }
            }
            if (!goalflag) {
                lp("\t \"" + rekkyo.get(i).toString() + "\"[shape=circle];");
            } else {
                lp("\t \"" + rekkyo.get(i).toString() + "\" [shape=doublecircle];");
            }
        }
        lp("");
        lp("\t empty -> \"" + starts.toString() + "\";");
        for (int i = 0; i < automa.size(); i++) {
            for (int j = 0; j < automa.get(i).size(); j++) {
                lp("\t \"" + rekkyo.get(i) + "\" -> \"" + automa.get(i).get(j) + "\" [label = \"" + usechar.get(j) + "\"];");
            }
        }
        lp("}");
    }

    static void cvtNFAtDFASht(ArrayList<ArrayList<HashSet<String>>> tmp, ArrayList<String> alphabets, ArrayList<ArrayList<String>> rekkyo, HashMap<String, State> beforeNFA) {// for graphiz
        ArrayList<String> starts = new ArrayList<String>();
        ArrayList<String> reachCheck = new ArrayList<String>();
        ArrayList<ArrayList<String>> reachablefromStart = new ArrayList<ArrayList<String>>();
        HashMap<ArrayList<String>, Integer> statetoNum = new HashMap<ArrayList<String>, Integer>();   //番号から状態の対応
        ArrayList<String> goals = new ArrayList<String>();
        boolean[] reachable = new boolean[tmp.size()];
        Arrays.fill(reachable, false);
        HashMap<ArrayList<String>, ArrayList<HashSet<String>>> findpathway = new HashMap<ArrayList<String>, ArrayList<HashSet<String>>>();        //スタート地点からreachableかどうか
        for (int i = 0; i < rekkyo.size(); i++) {
            statetoNum.put(rekkyo.get(i), i);
        }
        for (int i = 0; i < tmp.size(); i++) {
            findpathway.put(rekkyo.get(i), tmp.get(i));
        }
        int startT = 0; //最初のスタート
        for (String stateName : beforeNFA.keySet()) {
            if (beforeNFA.get(stateName).isStart) {
                starts.add(stateName);
                reachCheck.add(stateName);
            }
            if (beforeNFA.get(stateName).isAccepted) {
                goals.add(stateName);
            }
        }
        startT = 0;
        while (startT < starts.size()) {
            String t = starts.get(startT);
            if (beforeNFA.get(t).transition.containsKey("EPS")) {
                ArrayList<String> epsS = beforeNFA.get(t).transition.get("EPS");
                for (String ss : epsS) {
                    starts.add(ss);
                    reachCheck.add(ss);
                }
            }
            startT++;
        }
        Collections.sort(starts);
        Collections.sort(reachCheck);
        reachablefromStart.add(reachCheck);     //到達可能の集合
        ArrayList<ArrayList<ArrayList<String>>> automa = new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<String> usechar = new ArrayList<String>();
        for (String s : alphabets) {
            if (!s.equals("EPS")) {
                usechar.add(s);
            }
        }
        for (int i = 0; i < tmp.size(); i++) {
            ArrayList<ArrayList<String>> tmptrans = new ArrayList<ArrayList<String>>();
            for (int j = 0; j < usechar.size(); j++) {
                ArrayList<String> t = new ArrayList<String>();
                for (String ss : tmp.get(i).get(j)) {
                    t.add(ss);
                }
                Collections.sort(t);
                tmptrans.add(t);
            }
            automa.add(tmptrans);
        }
        startT = 0;
        while (startT < reachablefromStart.size()) {
            ArrayList<String> t = reachablefromStart.get(startT);
            int index = statetoNum.get(t);
            if (!reachable[index]) {
                reachable[index] = true;
                for (int j = 0; j < automa.get(index).size(); j++) {
                    reachablefromStart.add(automa.get(index).get(j));
                }
            }
            startT++;
        }
        HashSet<ArrayList<String>> reduceDup = new HashSet<ArrayList<String>>();
        for (ArrayList<String> a : reachablefromStart) {
            reduceDup.add(a);
        }
        reachablefromStart = new ArrayList<ArrayList<String>>();
        for (ArrayList<String> a : reduceDup) {
            reachablefromStart.add(a);
        }

        lp("digraph G {");
        lp("\tempty [label = \"\" shape = plaintext];");
        lp("");
        for (int i = 0; i < rekkyo.size(); i++) {
            boolean goalflag = false;
            for (int j = 0; j < rekkyo.get(i).size(); j++) {
                for (String ss : goals) {
                    if (rekkyo.get(i).get(j).equals(ss)) {
                        goalflag = true;
                    }
                }
            }
            if (reachable[i]) {
                if (!goalflag) {
                    lp("\t \"" + rekkyo.get(i).toString() + "\"[shape=circle];");
                } else {
                    lp("\t \"" + rekkyo.get(i).toString() + "\" [shape=doublecircle];");
                }
            }
        }
        lp("");
        lp("\t empty -> \"" + starts.toString() + "\";");
        for (int i = 0; i < automa.size(); i++) {
            if (reachable[i]) {
                for (int j = 0; j < automa.get(i).size(); j++) {
                    lp("\t \"" + rekkyo.get(i) + "\" -> \"" + automa.get(i).get(j) + "\" [label = \"" + usechar.get(j) + "\"];");
                }
            }
        }
        lp("}");
    }

    private static ArrayList<Integer> calcBits(int num) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        int count = 0;
        while (num > 0) {
            if (num % 2 == 1) {
                ret.add(count);
            }
            num /= 2;
            count++;
        }
        Collections.sort(ret);      //ソートしておかないと一定の規則に従って並ばない
        return ret;
    }

    private static class Path {
        int readchar;       //計算している文字位置
        int fromDepth;      //1つ前の深さ
        int nowDepth;       //今の深さ
        String fromState;       //1つ前のstate
        String nowState;    //現在居るstate

        Path(int readchar, int fromDepth, int nowDepth, String fromState, String nowState) {
            this.readchar = readchar;
            this.fromDepth = fromDepth;
            this.nowDepth = nowDepth;
            this.fromState = fromState;
            this.nowState = nowState;
        }
    }

    static void lp(Object o) {
        System.out.println(o);
    }

    private static class State {    // \delta , q , F  を定義
        String name = "";     //状態遷移図のときの○の中身
        int depth = 0;        //オートマトンの遷移の深さ　図を書くときに要るかも
        boolean isAccepted = false;   //◎かどうか
        boolean isStart = false;      //始点かどうか
        HashMap<String, ArrayList<String>> transition;  //遷移先   入力文字と遷移先

        State(String name, int depth, boolean isAccepted, boolean isStart) {
            this.name = name;
            this.depth = depth;
            this.isAccepted = isAccepted;
            this.isStart = isStart;
            this.transition = new HashMap<String, ArrayList<String>>();     //
        }

        void addTransition(String readchar, String destination) {//HashMap<String,String>だと読み込む文字(key)の行き先を複数指定できず、DFAしか実装できない hashmap<String,ArrayList<string(行き先)>>にした
            if (!this.transition.containsKey(readchar)) {        //読み込む文字が未定義の場合
                if ((this.transition.get(readchar)) == null || (this.transition.get(readchar)).size() == 0) {
                    ArrayList<String> tmp = new ArrayList<String>();
                    tmp.add(destination);
                    this.transition.put(readchar, tmp);
                }
            } else {
                ArrayList<String> tmp = this.transition.get(readchar);
                tmp.add(destination);
                this.transition.put(readchar, tmp);
            }
        }
    }
}


/*

NFAサンプル1


3 0 1 EPS
12 16
a false true
b false false
c false false
d false false
e false false
f false false
g false false
h false false
i false false
j false false
k false false
l true false
a EPS b
a EPS c
a 1 f
a 0 g
b EPS d
c EPS e
d 1 h
d 0 i
e 0 j
e 1 k
h 1 l
i 1 l
f 1 l
g 1 l
j 1 l
k 1 l
1111

3 0 1 EPS
4 9
1 false true
2 false false
3 false false
4 true false
1 0 1
1 1 1
1 1 2
2 0 3
2 1 3
2 EPS 3
3 0 4
3 1 4
3 EPS 4
000100

郵便番号を受理するNFA

11 0 1 2 3 4 5 6 7 8 9 -
9 81
0 false true
1 false false
2 false false
3 false false
- false false
4 false false
5 false false
6 false false
7 true false
0 0 1
0 1 1
0 2 1
0 3 1
0 4 1
0 5 1
0 6 1
0 7 1
0 8 1
0 9 1
1 0 2
1 1 2
1 2 2
1 3 2
1 4 2
1 5 2
1 6 2
1 7 2
1 8 2
1 9 2
2 0 3
2 1 3
2 2 3
2 3 3
2 4 3
2 5 3
2 6 3
2 7 3
2 8 3
2 9 3
3 0 4
3 1 4
3 2 4
3 3 4
3 4 4
3 5 4
3 6 4
3 7 4
3 8 4
3 9 4
3 - -
- 0 4
- 1 4
- 2 4
- 3 4
- 4 4
- 5 4
- 6 4
- 7 4
- 8 4
- 9 4
4 0 5
4 1 5
4 2 5
4 3 5
4 4 5
4 5 5
4 6 5
4 7 5
4 8 5
4 9 5
5 0 6
5 1 6
5 2 6
5 3 6
5 4 6
5 5 6
5 6 6
5 7 6
5 8 6
5 9 6
6 0 7
6 1 7
6 2 7
6 3 7
6 4 7
6 5 7
6 6 7
6 7 7
6 8 7
6 9 7





フォーマット

使う文字の種類 文字の列挙(スペース区切り)
状態数 遷移数
頂点名 受理状態かどうか 開始地点かどうか
︙

遷移元の状態名 文字 遷移先の状態名
︙

処理させる文字列
空文字列のεは[EPS]で表す
 */



