package test;

import org.junit.Assert;
import org.junit.Test;
import main.MatrixConverter;

import java.util.HashSet;
import java.util.Set;

public class MatrixConverterTest {

    @Test
    public void testSetToMatrix() {
        // let us assume we have the following Set of Duplicates: { {1,2}, {4,5}, {1,3}, {1,5} }
        Set<Set<Integer>> duplicates = new HashSet<>();

        Set<Integer> d1 = new HashSet<Integer>();
        d1.add(1);
        d1.add(2);

        Set<Integer> d2 = new HashSet<Integer>();
        d2.add(4);
        d2.add(5);

        Set<Integer> d3 = new HashSet<Integer>();
        d3.add(1);
        d3.add(3);

        Set<Integer> d4 = new HashSet<Integer>();
        d4.add(1);
        d4.add(5);

        duplicates.add(d1);
        duplicates.add(d2);
        duplicates.add(d3);
        duplicates.add(d4);

        // as output we should get matrix
        // 0 ∞ ∞ ∞ ∞ ∞
        // ∞ 0 1 1 ∞ 1
        // ∞ 1 0 ∞ ∞ ∞
        // ∞ 1 ∞ 0 ∞ ∞
        // ∞ ∞ ∞ ∞ 0 1
        // ∞ 1 ∞ ∞ 1 0

        int[][] expected = new int[6][6];

        int [] line1 = {0                   , Integer.MAX_VALUE , Integer.MAX_VALUE, Integer.MAX_VALUE , Integer.MAX_VALUE  , Integer.MAX_VALUE};
        int [] line2 = {Integer.MAX_VALUE   , 0                 , 1                , 1                 , Integer.MAX_VALUE  , 1};
        int [] line3 = {Integer.MAX_VALUE   , 1                 , 0                , Integer.MAX_VALUE , Integer.MAX_VALUE  , Integer.MAX_VALUE};
        int [] line4 = {Integer.MAX_VALUE   , 1                 , Integer.MAX_VALUE, 0                 , Integer.MAX_VALUE  , Integer.MAX_VALUE};
        int [] line5 = {Integer.MAX_VALUE   , Integer.MAX_VALUE , Integer.MAX_VALUE, Integer.MAX_VALUE , 0                  , 1};
        int [] line6 = {Integer.MAX_VALUE   , 1                 , Integer.MAX_VALUE, Integer.MAX_VALUE , 1                  , 0};

        expected[0] = line1;
        expected[1] = line2;
        expected[2] = line3;
        expected[3] = line4;
        expected[4] = line5;
        expected[5] = line6;

        int[][] result = MatrixConverter.duplicateSetToMatrix(duplicates);

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                Assert.assertTrue(result[i][j] == expected[i][j]);
            }
        }

    }




}
