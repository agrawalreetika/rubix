/**
 * Copyright (c) 2019. Qubole Inc
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. See accompanying LICENSE file.
 */
package com.qubole.rubix.spi;

/**
 * Created by sakshia on 27/9/16.
 */

import com.google.common.annotations.VisibleForTesting;
import com.qubole.rubix.spi.fop.Poolable;
import com.qubole.rubix.spi.thrift.BookKeeperService;
import com.qubole.rubix.spi.thrift.CacheStatusRequest;
import com.qubole.rubix.spi.thrift.CacheStatusResponse;
import com.qubole.rubix.spi.thrift.FileInfo;
import com.qubole.rubix.spi.thrift.HeartbeatStatus;
import com.qubole.rubix.spi.thrift.ReadResponse;
import org.apache.hadoop.conf.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransport;

import java.util.Map;
import java.util.concurrent.Callable;

public class RetryingPooledBookkeeperClient
        extends RetryingPooledThriftClient
        implements BookKeeperService.Iface
{
  @VisibleForTesting
  public RetryingPooledBookkeeperClient()
  {
    super(1, null, null, null);
  }

  public RetryingPooledBookkeeperClient(Poolable<TTransport> transportPoolable, String host, Configuration conf)
  {
    super(CacheConfig.getMaxRetries(conf), conf, host, transportPoolable);
  }

  public TServiceClient setupClient(Poolable<TTransport> transportPoolable)
  {
    return new BookKeeperService.Client(new TBinaryProtocol(transportPoolable.getObject()));
  }

  private BookKeeperService.Client client()
  {
    return (BookKeeperService.Client) client;
  }

  @Override
  public CacheStatusResponse getCacheStatus(final CacheStatusRequest request) throws TException
  {
    return retryConnection(() -> client().getCacheStatus(request));
  }

  @Override
  public void setAllCached(final String remotePath, final long fileLength, final long lastModified,
                           final long startBlock, final long endBlock, final int generationNumber) throws TException
  {
    retryConnection((Callable<Void>) () -> {
      client().setAllCached(remotePath, fileLength, lastModified, startBlock, endBlock, generationNumber);
      return null;
    });
  }

  @Override
  public Map<String, Double> getCacheMetrics()
          throws TException
  {
    return retryConnection(() -> client().getCacheMetrics());
  }

  @Override
  public Map<String, Long> getReadRequestChainStats()
          throws TException
  {
    return retryConnection(() -> client().getReadRequestChainStats());
  }

  @Override
  public ReadResponse readData(final String path, final long readStart, final int length, final long fileSize, final long lastModified, final int clusterType)
          throws TException
  {
    return retryConnection(() -> client().readData(path, readStart, length, fileSize, lastModified, clusterType));
  }

  @Override
  public void handleHeartbeat(final String workerHostname, final HeartbeatStatus heartbeatStatus) throws TException
  {
    retryConnection((Callable<Void>) () -> {
      client().handleHeartbeat(workerHostname, heartbeatStatus);
      return null;
    });
  }

  @Override
  public FileInfo getFileInfo(final String remotePath)
          throws TException
  {
    return retryConnection(() -> client().getFileInfo(remotePath));
  }

  @Override
  public boolean isBookKeeperAlive()
          throws TException
  {
    return retryConnection(() -> client().isBookKeeperAlive());
  }

  @Override
  public void invalidateFileMetadata(final String remotePath)
          throws TException
  {
    retryConnection((Callable<Void>) () -> {
      client().invalidateFileMetadata(remotePath);
      return null;
    });
  }
}
