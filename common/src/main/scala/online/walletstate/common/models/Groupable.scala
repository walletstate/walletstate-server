package online.walletstate.common.models

trait Groupable {
  def group: Group.Id
  def idx: Int
}
