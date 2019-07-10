package test;

import main.DFWPosition;
import main.SubMatrix;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SubMatrixTest {

    private int[][] testMatrix = new int[6][6];

    @Before
    public void before() {
        //                                  y
        //                       -----------------------------
        this.testMatrix[0] = new int[]{0, 1, 2, 3, 4, 5}; // |
        this.testMatrix[1] = new int[]{1, 2, 3, 4, 5, 6}; // |
        this.testMatrix[2] = new int[]{2, 3, 4, 5, 6, 7}; // |  x
        this.testMatrix[3] = new int[]{3, 4, 5, 6, 7, 8}; // |
        this.testMatrix[4] = new int[]{4, 5, 6, 7, 8, 9}; // |
        this.testMatrix[5] = new int[]{5, 6, 7, 8, 9, 10};// |

    }

    @Test
    public void testFill() {
        SubMatrix result = new SubMatrix(testMatrix, 1, 1, 2);

        int [][] expected = new int [2][2];
        expected[0] = new int[]{2, 3};
        expected[1] = new int[]{3, 4};

        Assert.assertArrayEquals(expected, result.getSubMatrix());
    }

    @Test
    public void testFillOverflow() {
        SubMatrix result = new SubMatrix(testMatrix, 4,4, 3);

        int [][] expected = new int [3][3];
        expected[0] = new int[]{8, 9, Integer.MAX_VALUE};
        expected[1] = new int[]{9, 10, Integer.MAX_VALUE};
        expected[2] = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

        Assert.assertArrayEquals(expected, result.getSubMatrix());
    }

    @Test
    public void testPairingPositionsX() {
        SubMatrix pivot = new SubMatrix(testMatrix, 2, 2, 2);
        SubMatrix dataBlock = new SubMatrix(testMatrix, 2, 0, 2);

        Set<DFWPosition> result = pivot.getPairingPositions(dataBlock);

        Set<DFWPosition> expected = new HashSet<>();
        expected.add(new DFWPosition(0,2));
        expected.add(new DFWPosition(4,2));

        Assert.assertTrue(result.containsAll(expected) && result.size() == expected.size());
    }

    @Test
    public void testPairingPositionsY() {
        SubMatrix pivot = new SubMatrix(testMatrix, 2, 2, 2);
        SubMatrix dataBlock = new SubMatrix(testMatrix, 0, 2, 2);

        Set<DFWPosition> result = pivot.getPairingPositions(dataBlock);

        Set<DFWPosition> expected = new HashSet<>();
        expected.add(new DFWPosition(2,0));
        expected.add(new DFWPosition(2,4));

        Assert.assertTrue(result.containsAll(expected) && result.size() == expected.size());
    }

    @Test
    public void testEmptyPairingPositions() {
        // uneven sizes
        SubMatrix pivot = new SubMatrix(testMatrix, 2, 2, 3);
        SubMatrix dataBlock = new SubMatrix(testMatrix, 0, 2, 2);
        Set<DFWPosition> result = pivot.getPairingPositions(dataBlock);

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testPairingPositionsEnd() {
        SubMatrix pivot = new SubMatrix(testMatrix, 4, 4, 2);
        SubMatrix dataBlock = new SubMatrix(testMatrix, 4, 2, 2);

        Set<DFWPosition> result = pivot.getPairingPositions(dataBlock);

        Set<DFWPosition> expected = new HashSet<>();
        expected.add(new DFWPosition(0,4));
        expected.add(new DFWPosition(2,4));

        Assert.assertTrue(result.containsAll(expected) && result.size() == expected.size());
    }

    @Test
    public void testGetMatrixValue() {
        SubMatrix sub = new SubMatrix(testMatrix, 2, 4, 2);

        Assert.assertEquals(6, sub.getMatrixValue(2, 4));
        Assert.assertEquals(7, sub.getMatrixValue(2, 5));
        Assert.assertEquals(7, sub.getMatrixValue(3, 4));
        Assert.assertEquals(8, sub.getMatrixValue(3, 5));
    }

    @Test
    public void testContains() {
        SubMatrix sub = new SubMatrix(testMatrix, 2, 4, 2);

        Assert.assertTrue(sub.contains(2, 4));
        Assert.assertTrue(sub.contains(2, 5));
        Assert.assertTrue(sub.contains(3, 4));
        Assert.assertTrue(sub.contains(3, 5));

        Assert.assertFalse(sub.contains(0, 0));
        Assert.assertFalse(sub.contains(3, 6));
        Assert.assertFalse(sub.contains(1, 4));
    }

    @Test
    public void testTarget() {
        SubMatrix pivot = new SubMatrix(testMatrix, 2, 2, 1);

        DFWPosition p1 = new DFWPosition(2, 5);
        DFWPosition p2 = new DFWPosition(4, 2);
        DFWPosition p3 = new DFWPosition(4, 5);

        DFWPosition p4 = new DFWPosition(2, 1);
        DFWPosition p5 = new DFWPosition(1, 2);
        DFWPosition p6 = new DFWPosition(1, 1);

        Assert.assertEquals(pivot.getTargetPosition(p1, p2), p3);
        Assert.assertEquals(pivot.getTargetPosition(p2, p1), p3);
        Assert.assertEquals(pivot.getTargetPosition(p4, p5), p6);
        Assert.assertEquals(pivot.getTargetPosition(p5, p4), p6);
    }

}