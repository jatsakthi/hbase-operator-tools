package org.apache.hbase.hbck1;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.RegionInfo;
import org.apache.hadoop.hbase.client.Table;
import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * hbck's local version of the MetaTableAccessor from the hbase repo
 */
@InterfaceAudience.Private
class HBCKMetaTableAccessor {

  private static final Logger LOG = LoggerFactory.getLogger(HBCKMetaTableAccessor.class);
  private static final Logger HBCKMETALOG = LoggerFactory.getLogger("org.apache.hadoop.hbase.META");

  public static List<RegionInfo> getMergeRegions(Cell[] cells) {
    if (cells == null) {
      return null;
    }
    List<RegionInfo> regionsToMerge = null;
    for (Cell cell: cells) {
      if (!isMergeQualifierPrefix(cell)) {
        continue;
      }
      // Ok. This cell is that of a info:merge* column.
      RegionInfo ri = RegionInfo.parseFromOrNull(cell.getValueArray(), cell.getValueOffset(),
          cell.getValueLength());
      if (ri != null) {
        if (regionsToMerge == null) {
          regionsToMerge = new ArrayList<>();
        }
        regionsToMerge.add(ri);
      }
    }
    return regionsToMerge;
  }

  private static boolean isMergeQualifierPrefix(Cell cell) {
    // Check to see if has family and that qualifier starts with the merge qualifier 'merge'
    return CellUtil.matchingFamily(cell, HConstants.CATALOG_FAMILY) &&
        HBCKPrivateCellUtil.qualifierStartsWith(cell, HBCKConstants.MERGE_QUALIFIER_PREFIX);
  }

  /**
   * Returns the column family used for meta columns.
   * @return HConstants.CATALOG_FAMILY.
   */
  public static byte[] getCatalogFamily() {
    return HConstants.CATALOG_FAMILY;
  }

  /**
   * Delete the passed <code>d</code> from the <code>hbase:meta</code> table.
   * @param connection connection we're using
   * @param d Delete to add to hbase:meta
   */
  private static void deleteFromMetaTable(final Connection connection, final Delete d)
      throws IOException {
    List<Delete> dels = new ArrayList<>(1);
    dels.add(d);
    deleteFromMetaTable(connection, dels);
  }

  /**
   * Callers should call close on the returned {@link Table} instance.
   * @param connection connection we're using to access Meta
   * @return An {@link Table} for <code>hbase:meta</code>
   */
  public static Table getMetaHTable(final Connection connection)
      throws IOException {
    // We used to pass whole CatalogTracker in here, now we just pass in Connection
    if (connection == null) {
      throw new NullPointerException("No connection");
    } else if (connection.isClosed()) {
      throw new IOException("connection is closed");
    }
    return connection.getTable(TableName.META_TABLE_NAME);
  }

  private static void debugLogMutations(List<? extends Mutation> mutations) throws IOException {
    if (!HBCKMETALOG.isDebugEnabled()) {
      return;
    }
    // Logging each mutation in separate line makes it easier to see diff between them visually
    // because of common starting indentation.
    for (Mutation mutation : mutations) {
      debugLogMutation(mutation);
    }
  }

  private static void debugLogMutation(Mutation p) throws IOException {
    HBCKMETALOG.debug("{} {}", p.getClass().getSimpleName(), p.toJSON());
  }

  /**
   * Delete the passed <code>deletes</code> from the <code>hbase:meta</code> table.
   * @param connection connection we're using
   * @param deletes Deletes to add to hbase:meta  This list should support #remove.
   */
  private static void deleteFromMetaTable(final Connection connection, final List<Delete> deletes)
      throws IOException {
    try (Table t = getMetaHTable(connection)) {
      debugLogMutations(deletes);
      t.delete(deletes);
    }
  }

  /**
   * Delete the passed <code>RegionInfo</code> from the <code>hbase:meta</code> table.
   * @param connection connection we're using
   * @param regionInfo the regionInfo to delete from the meta table
   * @throws IOException
   */
  public static void deleteRegionInfo(Connection connection, RegionInfo regionInfo)
      throws IOException {
    Delete delete = new Delete(regionInfo.getRegionName());
    delete.addFamily(getCatalogFamily(), HConstants.LATEST_TIMESTAMP);
    deleteFromMetaTable(connection, delete);
    LOG.info("Deleted " + regionInfo.getRegionNameAsString());
  }
}
