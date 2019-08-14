package org.apache.hbase.hbck1;

import org.apache.hadoop.hbase.util.Bytes;
import org.apache.yetus.audience.InterfaceAudience;

/**
 * HBCKConstants holds a bunch of HBase(HBCK)-related constants.
 * hbck's local version of the HConstants from the hbase repo.
 */
@InterfaceAudience.Private
final class HBCKConstants {
  /**
   * Merge qualifier prefix.
   * We used to only allow two regions merge; mergeA and mergeB.
   * Now we allow many to merge. Each region to merge will be referenced
   * in a column whose qualifier starts with this define.
   */
  public static final String MERGE_QUALIFIER_PREFIX_STR = "merge";

  public static final byte [] MERGE_QUALIFIER_PREFIX =
      Bytes.toBytes(MERGE_QUALIFIER_PREFIX_STR);
}