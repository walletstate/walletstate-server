package online.walletstate.models

trait Groupable {
  def group: Group.Id
  def idx: Int
}
