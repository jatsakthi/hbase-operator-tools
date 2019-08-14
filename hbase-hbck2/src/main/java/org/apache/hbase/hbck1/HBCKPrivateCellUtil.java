package org.apache.hbase.hbck1;

import org.apache.hadoop.hbase.ByteBufferExtendedCell;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.util.ByteBufferUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.yetus.audience.InterfaceAudience;

/**
 * hbck's local version of PrivateCellUtil from the hbase repo
 */
@InterfaceAudience.Private
final class HBCKPrivateCellUtil {
  /**
   * Finds if the start of the qualifier part of the Cell matches <code>buf</code>
   * @param left the cell with which we need to match the qualifier
   * @param startsWith the serialized keyvalue format byte[]
   * @return true if the qualifier have same staring characters, false otherwise
   */
  public static boolean qualifierStartsWith(final Cell left, final byte[] startsWith) {
    if (startsWith == null || startsWith.length == 0) {
      throw new IllegalArgumentException("Cannot pass an empty startsWith");
    }
    if (left instanceof ByteBufferExtendedCell) {
      return ByteBufferUtils.equals(((ByteBufferExtendedCell) left).getQualifierByteBuffer(),
          ((ByteBufferExtendedCell) left).getQualifierPosition(), startsWith.length,
          startsWith, 0, startsWith.length);
    }
    return Bytes.equals(left.getQualifierArray(), left.getQualifierOffset(),
        startsWith.length, startsWith, 0, startsWith.length);
  }
}