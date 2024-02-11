package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.{Icon, Wallet}
import zio.{Task, ZLayer}

trait IconsService {
  def create(wallet: Wallet.Id, content: String): Task[Icon]
  def get(wallet: Wallet.Id, id: Icon.Id): Task[String]
  def listIds(wallet: Wallet.Id): Task[Seq[Icon.Id]]
}

final case class IconsServiceDBLive(quill: WalletStateQuillContext) extends IconsService {
  import io.getquill.*
  import quill.{*, given}

  // TODO Move to config
  private val NotFoundImage =
    "data:image/webp;base64,UklGRiQnAABXRUJQVlA4TBgnAAAv/8A/EE04kiS3bQq7o4jy/x9sGoDTPaL/ExBFoqpipUvDdmun5JGdgmOYF45xFrbneNe+yMyxCBT5ntDD75Jtvycz8y35rvwnUDhndljZruh5AfDkmioBELG9IRauZWtvPSS5MIgkxNxmSwLGvBAw5BcCRlwQ0PN6JTANOgK4Sm5JMb1wW9u2lOzQ0P6LczHHLCIix//e51kBHEm2XTVdP8JDSEjE/tcjH3pp5P0ruI0kOZJCPGmxFhy/+/ffkhWfLAYlPikMzoT+TwBi8UhVVZX6qq+qHuuxKkW3qKoqIoKmx6qqdOisqtJsAaKqKVWVMALus6oJ93+3n+JSVQ0Gy3ECF09/r8Ha2M9+BgsuH6yDn59hLDvXYRgLgOXNBCqH8QMsX0XpYMLe9jVrzn0ft9NxonhfzDLHeWxm1bF6PR2rOLJWsXKuOh24R4QtQwgJeDcEhJ2QgBAGI3oOiNkZxC4kYCdRnpAGIQTZ57UM2EnTlJSIZMAghGEMBtJPkAMbGx8EvXZy/QLgiZ0CV/CvDsqekzI8KcOTrifSA7jiCvwRAHYAPCeCWKSN/b9FkiTnieSi7qke6B6epWNmZmZmZmZmZmZmZmZeZmYanu6Z6sLsquR4UdVUGZnx9nt3dptqdXMeh2rLkLco3Zzbql2Nw9Xa7YPRY2ypZjXqxT5mKOOjhVKZ7XX08ZXpIBfUhnYYjx9rpFFosW3F8fKuRnnMfKmF0ZL82rat2rZrWzHV1sfkOcVoMstGS5vhC7Y31mbe+weksazNvLfFzEze/oDtsqdtiVlaOGbvtfixbVu1bUu2culz0WWQ0JiPaKJJzGymWhAYNKYQUABMMs1ECwMzm8bMZnvvc/aaa1YK+P+vc5L39/f9rWhzx+g6JQaYNNjd3d3d3d3d3TpFusEgt9ELpA8YdbOVWNz/9/tOgECohK+6kpjcosiT90dBYAx/4D/4dX2eKhx0Ugvo2GGpfHVut5Z5dSpWKkVBJG1zLWWmZYSiqPIiEUMSKSiBGSvJismZaRDalSBb0n8ZmiK1PcGWgexMsDnj9nuX2/ZktryDNQgCw5def428m2/W5WUFosQOvtp10oI+QCcKIsCbRx9wbqnO2i+ldCy9ItY9ki6GusSqpaAgXsQbUWEWG02jFL7NDN82sUEYgBCGYYBRuPeKwL2u7BZCAQqGmeYCu8zaUKJOkMlXJmtrurX60F62CRKAZ8Wdr4sbIsiq7tWujyRwp1khCMBT//5NXnzVoVMHtdaAiPU1qjRG1yhFyRAPG4tFMZTEhBdiIgEBAasqkUgjhkADA7lyFKJGpMNvZotiGgtWV1JNsWqztbyXZW83bRZE4D2ub3phke1EWZ296srtghsD3mBNn3FOemENSTA4aodGlU5YUUqYHWaLRqkslJNBEAAYERMLjMDqmiDrGkBKKetTR8aMWGFARuRpzHfWlVSet0V9Vf6+7leNAS/Vsz/CiV619Wr/+OLe9o+vGgIXsw47MyLRGR6zI2LVKyV/29/8zVLFcFLgAgiMmAl11r5uTEqQ3mWmDLlYuO1thioUWFPszM5objezBVuB03R0KeZVT6/8z1d5yz+9KngUrHRBjo3p6HgNSal1IuTFb5oVTOLcHNR10GA0K1nXIEMjzkBvfNscZNiZV2UhMweYJqgCFkuXJcqq6A3/6qqf9ItXQYav/M/XgW/4V9dHY3V83A5uK+XrSwliRLM0ILiMqlOeumGWiGHI8FoRNFGwgpjvLM0zOc0EwVJ47CZnclxY7bzKP+X2/RKB8BPeW4/X/5uXOxc9Z4FBPNXnLStBLpiVqCloqDrlrRuTEkG8jJBPwAlpyGdRTqO7bYxgHezd+7WEq2Ze95mjD1jm8Z6zFg/Kya05P17HtFfRt2U7CEsm5zAQTRMr1DQ1nhH7s0njXmka88wa1Nfnt4mCXTzjunfnMwmrlDe8PhpCT9tvJy5JOue3pVvMyDiYSSbnaOrAxTqVwJoa7zKG/Of91CCbhvRdXz4XrIRFpmEV8hbXaQjdddQCrovXKR3J+818BT1FXdPEajV1TViPSX98gFyeiQO8L5gBnaVhlfEWz6QhL/MHL/5VXM5t4eaWzqAUXNpMYuICGqqONevGpJRGooH/P1kxaIuG6o1391ImyPFCXTSsIt7s+mjIR1fRY9rlKd3U0XobIlvx5Cnquo6V67omvFYSt2GA1UP15hndPvslHqOjYZXwhtdpyCdW0WPYlS3ctiA9NuYqqDkaqo7V64bCu0y8fNToZ8NgvX56PvolHjUaVgGve829VL9hMv4Hv62vc92VrXXPwurxg3mSybm6VHUqoW4IRHuXiZuPdugjM9RefPnf95EgMb/3y+X15juk0r3KP5EH+tRxIRflrDYeXaB+pcL/3bmGpk6l1NS1/Np4fKRDv9uKdD0h+J5TfuGQvkcq2q4dDTkfw1p0fTv9NviTc5dRGAiVVONOCqJBTJz5DQP39mvPAw8L6mSrVFas134mHeMMVtKRy1McES1yCZNTDUWTSqtpKkFwU0/p0FX8XNdqsWCHHFqprEgvUbJHAbvp7GSd3hoyMj3XkNSpxNowhESHBtoaKXS6xetPQQV7ruMuHirQLnEh57VBLTmvo+15KZNkqq5VnUqtaeoxs7QlhYVOuwFqBVtkrLTSvOh3J3v8L3h9y++oM3/AFMUkU3VJnUqu3XrwmVE5Mx2N+u+DbM7YHqG3pKLs+kku5LkYmrAT20S2zIQ5FJVeUw+84jryT/PsuW9VEe4iV0E6P3WEZ+/mWr/WccnqUwzCkqmGpl7xqEM9MJJESo3au7VOeNdSOiOV4pU+eOSh/+pV4BU+ckrjL51jWr6GXNnv1iV1qsI6jboqlbRAvI56xerHQ/c69CxSETrMSdptWMz6lcTl51BUk83gt6LSYvY2a6VgEekuKq2vvYQPsVYJRrU5eXljsi5pVhNN3LopM+NS0tyy63SaJmjuXBe1uhcr2b1L5IGrV6wGRkKpkjlMoPqsIxIS5XLJb8uvgu17lB2KWNpOEo+WzsCodf9BGVCd1hGJkWgx7YqdRQjCXS0shUC8Sls6/jaTdUm9GqnTqMu5fxlflUKyxLLiEV7HB1rH6BODUsIcYgbVa52/csc7X6DTmn9kUSnBqaw4ap3xAswOVLN1PKLWad8uKVIOltQRo71ShlIlUAHUq5k69UBt+TPtVswCzII+5Jmv0PB8OrOTss/KKzHipRIRqttrNwRiksTLZwg9OU8w+kE6zmpe+N57cqr1T1F+dJ+4ZlHeN4qighE1OyYGX0e1e21RNE1CtFR12/WC907Zv9ryiKVEg+Rj+uS9u/DcD5ac8OdMxwiofiVGk6tuuuMRdT9ym4UYAMLa7qb7/76JiY6TBFTDkkDMolGSQIS1gaMsYwuYyiNLvKaQkwRUx5JAWDQwTBXAFosoFFDqeNMoycyOC2RA9SwDueXbzCgUwBLyBpQsFB2KHTYGAguopmWwsbgO/66YAJeyAHnqP/1VoXT10lQ4obhu/MPiARlQXcviaRROo3i8UDhG6eq5ifv9g18TWX5P+z1p+HE3594lnedyh2h25CQpEKptGQgzQ5bw5JZ+49X9guce/89+XMuu7/F/ER7wcte2/9/lvfzOROz7ZUD1LQNhUcyRxOnIwNt+7zrB+72P/5syK73/N4Wlb9lZuz86P0TmGbNLnJMB1bgMBNHAldxz6GedTekDvyllNRgoJLv4gV9Ta1rYlyCkMqA6l4GwEDtNM7eF4sgyBGWUizEw6kv5v93zHrn1GAzUlFG1UTcVEtHvHkiWHFY2GYwLtaw5stEzhq4+kBjytxPGkVTrWoi6SXKzn73IWj1Di5XJcjMEOwqpu5mQGeRtmT+S6l0b4SR5s8JS942sKwu5173fchSyEtdsfK86kmpe3+vKhqDASh6+P1ZZDge+Y44c1fl7EHEyoNrXgbzsXfKjmoPfRsqgL8YgVSvuLERQiS2UkaEr7i5JH6roW7NRN/BVkXO2tcVk4i/DwyZ6xxTZcO62Fj3K5Bv91hfLN+azPPYU30vNbGYHrlvje2O3yC/LvvH1f/OIHF2nffC9SpbZxSsI5LouNtG9bjCdZQ6nuz8dVV2fZQx8/cwbuL9cJF4G2MZARvADD5Yb+Jp5HPFZNoqWfarIS77hJ2sLT9UmXoKHrfQSL+InX/KNt1qWfVocNfIZr19pmLttZtSV76V2Xftw3RrfG4Hcy2aGz3B9VI6WAx8P0kxd8RgG3sRcbKWrjh58hHSmcs9RcviIvLNreYaViVviBdjMpvfT+m26lvPfRoX8uB98UkFTdpkcPcNQxIbe5WbZZdIdPvaHnlSOhgMeDvrGj+lHONpsttnE7IbefN2jBx7heA58MEZBiKAim4rkSyQMsKFB+GM5KgSVhGvvFdajMpgyY+CaAVtqNjZvGqSMV/upR7/W3vxjPywc2r7ve+if6ORvcuR17ch1j/Rim+976O9DWhmdv/pNsrZO+9GvdIJtWcalnodQwKbWCJMv4DKC7Q/x9A/cWnrHb33HeTvdMaz9jstX3M+EeVmzJyZmgGqYmxz83GeGc6EvsrVjehCBvfd3Xn5uvY3BO5K/pZv2BN00SVpv3/jl6+M/C+a/6jsSb22827d/mQpC9uzsnXL40J2CQPWR2FWNQYzvZ13xde/ybo5bG0y9z3e+U7VJHamxL02TkDq/pwPPr/Z5H7wdS7jm3uc739FwxdOu2fPk/cxVyZQ+ad62sCUwSNz0N7wZ7vqmdM3xNO98aPHkb7uv6aD77252XeyMqZhYfO8L1n/sX7x8Rbim3u+739EwfeuWPddeH7xzn/wZbTqCnZXCRChGYfA5rwl2v++DR11TPNU7H1o8+VvvazqO0z1HNOfY+Em2ZkvNHDF+8N0vyHzMX7x8Qrhm3vvVn6dh+rluXPiw7kPnS2KWlydhb7cEhpLUFX/oF3aj4J22RzRcI+GrvP5v8l75Z6rOORQuRHR0i+05ScxC8m7a/42tfLm7t/6ChDX5Ng8+T8MX++nObbvf/oPPlUwRYHclYfIOv/RTXST4Ovrqq4ZrIMR4TbfkTsgeZEraoCBM3aM937BQMIg1+Db//kUaDh2O7vy/c0hxFcUQAuyv5Lejk1v6/+cYwcyWudPVe7x/ezLvVH/zcN3ifc5IK5mySWG7i1929e6PgrMe8PQht1rv/Ke/4gQxz/7JSycPiSk3wA5LM0SRbLfc/ut1f8GqvZ77Abc6n/ErP6HhMnt2p1XeChIRzBU2CYPQ1p0/2JtnBQ/c7+f/QVZHfpqe6nY2JvDXNiSG2LzZDoFJTMYNsPGjdn3D1fjg3/5zlb3cXzPKtMYma4yKCemBnMcDn35eV+3FegU9NaENDQNssw7byx+uCTwWcZXe8c/+WuRb/dddz95qjMpLaZ+kCVFJthrOpR5yz6tfkFV5rDdTKW53QaowKihtnzS3bi2/pE5nL9yqSPZ85eX2Frx3Anb6qq2lyXEm4Sq865/8pZPv+58O2O8bb46KlGptpzSRkkQN+oB1Gci+p593K3vcN+Rk3i5qvb1xa2zWrRcinc4p7F2yspBETvxBr4m9vmqxSOQkwpW8y48fx8f9x/v7JzgwW2htrzR3/8sHVil7X/2MG2kPHJ0ct0Duiih7paRZyQLpohzH7400RkJHM2kAdnuS4xkb4d1+/AjnWeuQyOBMmNN2S89lQ0KDz5MS9j39rAxL4VisUW1qVVLAflHshNa0XqKRpOKGPbigxRmRFCcH2O7g5JRoqZE82GBY+DK/MR/LqLtPGTH79UVTOfGZL/SbP6sh8C4/foTn3SedUK98CJT9UkEuJJR+ictJs+/pJwQ6ouxSQ1ubNxMwIvarzi//5M4aSlspPMycSJJBCXFygA0PTv6qVA7loRTBu7/f5KI69FJTl7Fjl5m6VKQG3f9vzXm8y08kvPFP6hCnlwyUHVOBjJN+45+ohH1OPyEJOXadc1AiRSVF6naszi8mKdrNOYhE3G4S2jKg1UEQsOnh1f87u0oe+xXvBaLqOz4pIhMT9mti4usmx6Pqx+M8fg4Ce9Xf+CH/lFNtzmHOGBZgx+Xcf46o9BV/8ce8IOH+0z6WLoXwdeY6gj0XZrMxujzoWbFgK7s56agKi47NK5s270VVtHi3n+BnWkj/x4yTCDSwafLyKXyKNESqZ1QUywR52HVB7mZR9YTi0j2zIr+IIQ1smjQrZFf8Q/a4+T5945/8xUXZC4qXZM62CWLmEub5dHmBX/wx9wJ3Kso8WdeBJ6PgxDnsuhfDZZ7o+govr+js//WNJed84XtaRskvlQW2TUZMitV09v/51vatjy8lxdc85htBDLNxIjLf8qiOqRTHLqO0xdvbOCGLHFyrW3Fxr3N0QSzBxB9p277MSIiuka9OqVMtoxNigI2/dnTQstayQCdzwYwYwr4ZyB1vURwdzZEyB8GEffuyYGtptE+kQBHYtwAVPxSKVduSCFz75gZbItameJZS2HpV2KXMpZXYI7BzwQ3NtDJSaBbY+g0GhZHkKXs3EVnyI+aw946xiLHg2Lx6A5sfMbD9yu5t2SqCvfP1t2LzzGElcq6921zamk0ajRn2zjXTaGw7xQpp5+TfM7Qz1rIutj4CsZNs6ei/yAx5i0227RbSCKX6Pz+aXCS2XeLmaYyM7Q1pDHHKz69Eju9YwJ+QjUi2R5ctxsZ3mYsJpPwAV7mA71QAZ5hyFVYov+u/RLNlqHbex6fcfcdnpY6rcBwLc3C2ugqfkR3fKTe+7j5DtStmm4tCbMp/KbNVngAUCseiHIUSC+A6I4zXKH/zYGJzV4hRl9nVF8AVG7coN8cfyccFfF81lO9Yj8NW/KtdzQWFM8wS/565uNsM6fe0s9WkKZKCCVVujCQKDMMHXBcfR+FYi+Mr/Kv57iYccObHnWFO+Un+Vvo4W6J321Z4alfiXnuam4lyZTeiX/ONhu83fPyG6/uuM2ElDspVOA22Os68mOHMOyNZoBiR/Lldsau2f+LfXhqL06aWBw4oYaXJAiswFhp33TDu7/DxXWi4vnLBsYwJB5+G2oo5sfWG29fjiIm7blXzViCFxR2lbcNH/ZGGsHRrk1O6ZFPl549vKYVioxZbiIY/jKwxQc31G77fwMeZsAYH3Ak24bB1/XZz44bHOCWjMFE0ihNRY5shXMcCQopPq/N4wr++ZfZa7ujf5QR5lH8JCo0MpKHf6ILuxlCjGOIrtuGLonE1FzZhlb4DDlvZ7mwXezjHlGbkJkobSVi4sYvohHaHVuOrIgO3/BDkFqzyDt5ehj0+ad1eyztDJCYalJ8Q041IKEIeVsBhc2P/wf5uDDVKRbRw/R2NHWyFDa4VKMAFZ/12sYcjdhQ2MjtiF+w5cUqx90RnkYB4UFvGFeUvDYUW/MG762H5fWv2HbMMbIMFjHhUBGLBFANutM5hKPQHs3Eznx3rHWAccMptuGIeZ/sxxUFuRvvGg+zDo+zDbg6Jxm3WTSisURoTJrJ/joRll7O6OBqSFBWxwC0/heODYlurw6KP49ieMNCIFFeedwAcrNFhvbPeWZ+fkdxxfzsecmJPEW3kg2oA7sR0rfyUwGJIRg2Hn7cazj3L5tnUyf4ZosQaFe4Ev1p6FknouIl7LjtQXCNWMM48uGXnTowwPz99ldJg98EDzGHnoiByQTVoqAbWKGXMIc/Gs7Z6Br50RJCUqDlWEJsWASgajY03aoNTiEWiONjqOM44Vuk4x/zFxMS+HRYXGwuNxgzgYpVN79jSqBnLvZycZ71WCqqdntXWASi38YMbdyl2nTjGNKZZRb/MxsdZ17axR5EI+YDrg4t16tnpQq3g3McbPaxjpKm+zhSW2sB3uU0L7DyIGlswxfxKylW5gIJj1pszOiY6B0o1UDSw1Kl0kaaKpbIckWXTkowaIji0lQCKRinMdGxEB1sdytwBasD8fOsgNbEO16eBpWqi5rKjYelYQm4WBcaz7cet+bH2WOlazHBFQswwsioPl+HTANOJQQw2gsJqXRkXOa159v20DcmbrRfnFVOZNMJJq2kA40RhfqTydPwRhs+7rsKKJ8PvKVml95ZdHZzOcWRGZRYMnLSa4c48G5Xru24N/G9Wo8xn5aIxzUZ815LkD11pSJWccY6DfgWWdcrTSgwUbUG40xSMktEAxhnujqoRfZ8atel82OhgyZq/e9zhKqe/AgiMN7mcNTnWHitdK2KiRiZcwZ3Hd4f5zmhzAfzaPX/QVdbkykTIseZVzpc1SKD/eKVjBWYnQzhpSYBaEcyGO+6zUn80KfD9GpHGULiRi0VPCoUbHBf2Ha8U4CwuMDjNyQx+SGqLclW6YRbTfg3fHzZ6FT6APx4RQ42MMYNFa2mY+5wVmsNZ5zBsSJFDNCfNjiiKZbu3GQjTOwDcUbUJqE1Pb5jJhs+cwbKIot9+6JjDUMVhEsHWHBWp4DWtSkUG2bCiYQhqfg3UaHFZuRHyIR3cccvCazFylAu2IbBhvP1x9WQ105gxq5UlKWAmYwyGaGPc52aM7mnfH58pNQYa8zVlVUrObvhy3v949ox48BRZPqb1Khp4pCWNOPOZ2cbM/PR0DfxRBEzP9zdMw/U3WZXm6L4Rlo/pHFRxJA9BTVZLru41rWvc/cl82CwCo9T3h/nU/Jo/sHAbFws3CffMsuT/443EQ3VRcrPJscGs1palaqSNYvFvRokC8AH8mc+UxhyUdWlmt2U1mYeMstJLzozlTOjBjsa6Xf+uJXF503DHR3CGOWtu5bXxzZ97pRlAWRUc3aPYPZvA/sNW5iFYlqXynl7TsoBayfhcVtWZWDMOuAqcERryToYDVt40Cff8j4LleCvjFF2U7BgzY2BaWxjuxhyszAUXHH/1Jhx8Rhp3b5M3ZrByzexMRmM4RZRVPGBEDmFst9MYweNa2XjDhI0jjKNgAmdiE/4qOb7jKjYxfBzUOsYtzVVf2e00HDyNZf85cVUE9mT7gyJYl7MZrSAIJmJdCkwB7rB5x3HYxASucn3HH8lRoHxQzDvj1MzBDFavwkv/rGD9k+0nkavCaY93Hf365kZNqS1rZPdYtooJc8JhHsUmwHGVO0y5Ch824ThixrzjOBvveEer0zRv1K9vOe3xnmOVQz5tn4l92phAZdPaplVEtE8YE5unx9dvX+84CphgFWtfg9rKPM72mirNaNmRGqiaxTVPTkT7tOFTtomEq8ZDRQW7V/Bt0jCIWlsatYlI2GniloXBN+zY0Wj4DfDdVRnfDtvXb1+/fvoqTmdoCQqL1xhYytxyfcbDlrKaFxzGEeOLbuW+EtfaQH3BrqFT9B/jG77Ab+xo+Dum/WE+DsD29eu3iz22H7M9P5EUi0VKrMPqXaL4PuV6p2c5P7Y63qutqxOsymjCpUOlrawBsG7n8IWHL+TCNxw1Dzvu6rMDpplnPc68c8PtjpPfUdpYPGNv8Q2/2rA6TdiO6xvjBZnnXXPe6nA6F6Gb94eMH8K1MmDC5Tb/MXaDAegN+fBtEaMxbzR8/JrvAOMO251N5kZuR36waMb9wWJjo8LyXX7oSt16iv0dx2qHCGb2TwtbzXmBiVkb0JiI3KDjJlI3IX8lA+rGRzXGfeaBu3LM+PFXyWyUNnbbeNAZ9wuJolDgTtCwNm0gXpv5I5xKwRQEtnosiZLmjZIwMI3F3xXcidKziN1gp0/EoptITfzF5QdyjeKKbUfNMM28Or4IYkZ8ok08wMYjzo0HMxaIG21pUAldIderp9kLXRMhz+6+rIeVKfNe08SsjRq+66vSs4jeYOEnYsnHo3uNpY2uNXoXBsJQ+M2J6UiRFG2wEBaJxbBbh6+59Nf/lBHcDTSsTje9FvwRrHx2d58iWZNL5qZj6fHWtlmN5Td8f5NP8VncMjqIwXzeKBo/tbkoGtFw1B3/2T9bd5u7f1aDHTSwfM1s3FyX8zyvGV0zIe/n/rMuJ5PES6mtbrhyJ0DRcBs0UExshAabJnCxfo2c6dL695q7zwhZsx/aVW86wGt3PFEgFQFQuBOsqjvhUhl1cOId0+M1QcNbeFHWcMj7zfm4i8yMRFeGSq3xZrpY/x778jHhmuI5u+pN+/V81BgLpJ2RwdgJ/eN5QcPz9ays8ZDHujx+3KXabz8ZbV80J7caXap5rP2+TwjXHM/cNOwbj6dMQ3sTJqO9eiJ8jvWirMXQRASj05rdjoRou6IJvyTN7HtPhmuDTz7eCufRcIE3zJDanmjJvIk/zzSMTz7eFdbug7mJYH4fn3zJWIAtNRDVLClZCDu3K3jQCGu5ZwgX3R7q0j9x8k5uajsCAv27LZx+0NQ+7O7I2mIf84LtX/89JypsaFPPFUayyPbcz+0c9jFlrY8heO1wVbTiBYmBaPuBq0zRnnMZYxTuFeVQ57bfj6DatR9yagiSDDntdii90tEQntJUUNk/Xmg3MUgEopr2oknTJNdp7MKZnMYNRuVnTJGX3JdHlmlli+WZBOxm08QVIJmTnu1y8sPzTTI6ZIepoOnwcf2g+LU5aS9cMrIW+rzWsSHsGcdoPbVgQR2/AEySpr1o/rMFxkKO51QGo7ZQEp7VxSV1dFFMopp2oamYvELSjj7l5dGF5x4yethLjg9a/1capzRrzGEj57LwNz7guPZjrzgBo3mJhIUZtpPlFZUE0i7IoEhyuc7ZcJaYMLof9PLBQMsYFS1k0y405Te02Ege9PIQGeXpEvlmc9cqnoGFwSSqWf01FZOFGfE64M0up5UgHoaMNjqH45EtHaN7TsxhC+duGLPuj7LWi53iGP2dETrSvwNfJATVnyb42Q7zHTaAThPK8RSLkaR/wvACWe3JwEtAsko5RYyyPMckIXHrg3KrPVdxRcKLSsqDxRFerLetYnQqigBVzSmCK8To/GK9bc1fo1zbZsJDdEtFaF8LqOqDQvjFh+hWTJsJ5ZtE2IniCK2/DwiqswAoQnTW+hr8Vcq5NRC34pnjA3CrMReC45PxcUtxHOW9KEa89o6DpmrXMRGvvVjUjHLfORChXcyQQXUWyN+PsAe7GJR/29P3GcasrSurM+kK2tBy/kDEAg9zRGJWvhDVWU1QgFw6iRUwJjHMj6CqJzVMBAMP18EaowCmEmEEEmS1JEEGYsY/g21YqKBJBVTVgboDlroOjPFfQTRl9SSb3yf4H9ZZCTeao5j/CsYJ4FZDLkTg10v8S5ArsFY5ePyPkB9/ZpRsk1S/GmQ0ZAj52Z8MnV6LxHKXC/KqL7Ej6iBVUO0ESsYGJf7PK0uXBCvuRuhJtsT2bYaQoKsXDdI9oUS2j3q6h2DNacHZR32BOgQqcasXFznH/zrnVM8KwKpzZgh25rXi7lGcRxXrRdGcyOs3wQ6yMqx7hQyys5r0SBKGgekAXX1oAj17nQGS7KiGwZlh5cuGlYYyoOqCMrEikLjVh2EggxsXyAykGsN0acewdnnAJHz6XNbmZ0uLM/6DR9VZw4uaPybP8tNPa7jEHJFYfs8wrjCUOb1qToZZHaCrB02gZ5OiV7khzbkCPRiVsIf4FnuO3jE/T210cLSHWz24eEef8Hm9zONN5lF6iVRGea7TK8dT7w/XZseMfpEyOVEH6GpAE+gTk5EBkWXmU1+eajj38UYlFXPZnuH11dQhVsTAIC9UTXSl0zRV+OvxGYOs6tMUj6UyKulBFV98XZSXuNxVZDSpx6EFXA+pK5uWeO4v905kx4TXn/sKXqSbHjIilVVe3AFB83LGZG3BdNx8CFpXLq1BKMTw05lUXE7QxMWEpOIePMVLHq8VwcIBlXXr7xQYZiEIM12ZtIRwvsX4v2mVfT2XOF7rIVOkEsuLOMD+4/shxpj4LwlNRdC68miNQExi+CXfMMS4/VUGF5uQVOjwA45njyB7nmloml+T0VYTQeIFoCuLRgZyrCXcK605/+87eMveakgFP4DwKI4iKO/Xe72jRhRJ/KynDQRdOTRN7RmKSfnIoIfaft4VLOCRogeNSGWX53LsufaT8LiXpzH9er17rPv9mRM93XR1ZdBuoL3ZmbsuJzPAG497+dAYnvPyYTk3Jqn43mmPNw7vXS+Xrwf0WpfW/34K73raBFdbn3YV3mzK/LYuMgO8/kFz+zrktMf/O4+qMFxiiqDx3fflkwG93KXfhNFq2gxRTT1uabqpCQ1cqznVrVXp8eYHzj4S7GZ3aUjVGJ71eKMIGp74fPdh33i6y6nMiHbcfzAIUiNk2qq0ayJSnhjBt8IQ9LMwzZsvf7mUCXKc7XilgkBVGb74cVXGBN8s54E+je6huTUkzRtYiGpqbT2apiZsJtCk0a1cRhN7Zu99DiyOhlSd4XOvq8LfPJiH+nmpa2ygHf0P9/U0aGUlSjY1noF95aXz0M/GtL47gi8EK+GaIVVp+JGnq3ukg0Ow+mOcp5frvuXTuG41fHsrPoLe10OitTVomhpv1sDfMwLdavzyAb759DkTbspr9Ow+w5GQ6rV7uAeIhHBW9djTTktxZpJBnebWDYl/pqbAANUsM+2aiMSbPfr/3LEHy7Iwox8PGWME6+FhohcY5lHlipk+8n6IEj5sPQ/Y2U5N5PgkhyxEp3+lSPBMVCAEs6pZJtqdlMjwh+6UCsaMXsWslubGxG4mCpbBk+4Py18IqYa7HNnPiwuBS0z9dqljk3Z0giHttLl6bkZWmIST5+agqQN3VCnZ1CDDHzLQBCRDZpBmR04VudmsLqYLqoBTdNEDRpRUzX3D7RcnSICzOSV72si22fBYDk/Qq428lHHcjYrEcHIugGBWNUeBdiclSEGcnfq16yThuDRJjjUFzVnB3EOcXwXbgIeSHjDMo8rumWSvpmPA050v+uonvRbbkBYbHOXQOOkkLdoNs0Nu8JtFMSZNJAiCYU0duKulZFMPk1LOBlMRJA7XcVeIjBpzrC1ZeUaLlqn89c5ntSAB3vV09QcpelTlfZIU7qG6EQKd5/fLuxxv2y+xQxbYgIh6R21AnK4xin45CT9VGBSgBBs9T04GBASrJJHI2WAu+PwIxOCOv3/cIBRozFNXojqv2qFRtUxLn2U/bvGA/tNrPcschoiSar5X0oF7hGYewPPu9OG7FO8+t3RH0nGlI3SPWhdDXZPWQlB4g4RxYzFj3cYrbFSmURIowCVibHZnosbMth2/nhO5iVKac9pp2oYidWJkspVZwerDptUXmmXHAF5vvbjTO65/ClKAHfz8xHBLmjzo5f1BMryzm77duhaumPbtbJ13JpWhU6e1zNGxoJJWCqK0yZASs1ZGFcYsP4pEkG2sK2FFrKBmc9YoameKbJH/B2mK2bYEW3q0q2XU9emPpdrWYlueZ+4aPIavmDs57/FGD5PliAKjEgI="

  override def create(wallet: Wallet.Id, content: String): Task[Icon] = for {
    icon <- Icon.make(wallet, content)
    _    <- run(insert(icon))
  } yield icon

  override def get(wallet: Wallet.Id, id: Icon.Id): Task[String] = for {
    icon <- run(selectIcon(wallet, id))
  } yield icon.headOption.map(_.content).getOrElse(NotFoundImage)

  override def listIds(wallet: Wallet.Id): Task[Seq[Icon.Id]] = run(selectIds(wallet))

  //  queries
  private inline def insert(icon: Icon) = quote(query[Icon].insertValue(lift(icon)).onConflictIgnore)
  private inline def selectIcon(wallet: Wallet.Id, id: Icon.Id) =
    quote(query[Icon].filter(_.id == lift(id))) //TODO Make icons available for all wallets
  private inline def selectIds(wallet: Wallet.Id) =
    quote(query[Icon].filter(_.wallet == lift(wallet)).map(_.id))
}

object IconsServiceDBLive {
  val layer = ZLayer.fromFunction(IconsServiceDBLive.apply _)
}