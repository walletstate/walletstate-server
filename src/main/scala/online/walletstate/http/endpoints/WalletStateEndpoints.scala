package online.walletstate.http.endpoints

import zio.Chunk
import zio.http.endpoint.Endpoint

trait WalletStateEndpoints {

  def endpointsMap: Map[String, Endpoint[_, _, _, _, _]] = Map.empty
  def endpoints: Chunk[Endpoint[_, _, _, _, _]]          = Chunk.from(endpointsMap.values)

}
