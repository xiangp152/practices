package test.armzs.compress;

import java.util.*;

/**
 * Created by xiangpeng on 2014/9/5.
 */
public class CompressTest {
    public void stringToChar(String s){
        char[] chars=s.toCharArray();
        List list=Arrays.asList(chars);
        HashSet<Character> set=new HashSet<Character>(list);
        for (Character character : set) {
            System.out.print(character+"   ");
        }
    }

    public static void main(String[] args) {
//        stringToChar("sdfsdfsdfsdf");

    }
}
