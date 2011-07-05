//
// Yarrgs - Java command line argument parsing with a hint of a sea breeze
// http://github.com/groves/yarrgs

package com.bungleton.yarrgs;

import java.util.List;

public class ListOption
{
    @Usage("Type of injury the pirate has")
    public List<Injury> injury;
}
