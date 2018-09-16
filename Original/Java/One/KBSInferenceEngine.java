// =======================================================================================================================================================================================================
// System: ZaraStar KBS: IE
// Module: KBSInferenceEngine.java
// Author: C.K.Harvey
// Copyright (c) 1983-2008 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

// HIGHLY EXPERIMENTAL

package org.zarastar.zarastar;

public class KBSInferenceEngine
{
  char IF = 'b';
  char AND = 'c';
  char OR = 'd';

  int[][] tree = new int[100][6];
  int ST_SIZE, UT_SIZE;
  int[][] ST_TABLE = new int[1][1];
  int[][] UT_TABLE = new int[1][1];
  int STATE_NUM;
  int treePtr, treeSize, rootPtr;
  int[][] rtmt = new int[1][1];
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // analyze structure of rules
  private int breakRule(String text, int[] numConds) throws Exception
  {
    // init
    int upto = text.length();

    int x = 0;
    int andOr = 0;

    numConds[0] = 0;

    while(x <= upto)
    {
      // check element

      // check conditions bit
      if(text.charAt(x) == IF || text.charAt(x) == AND || text.charAt(x) == OR)
        ++numConds[0]; 

      // check and/or bit
      if(text.charAt(x) == AND)
        andOr = 1;
      else andOr = 0; // OR

      // step to next relevant element
      while(text.charAt(x) != '\n' && x != upto)
        ++x;
      ++x;
    }

    return andOr;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // searches TREE for statement number
  private int scanStates(int stateNum) throws Exception
  {
    int x = 0, done = 0, result = 0;

    while(done != 1)
    {
      if(x == ST_SIZE)
      {
        result = 0;
        done = 1;
      }
      else
      {
        if(ST_TABLE[x][0] == STATE_NUM)
        {
          result = ST_TABLE[x][1];
          done = 1;
        }
        else ++x;
      }
    }

    return result;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // scans and joins trees
  private int srchExtendTree(int nodePtr, int partition, int topRoot, int[] created) throws Exception
  {
    int firstTime = 1;
    int quit = 0;
    int recreated = 0;
    int[] uptoPtr = new int[1];  uptoPtr[0] = 0;
    int ruleNum;
    String[] text = new String[1];
    String newClause, oldClause, oldText;
    int clauseNum;
    int rootPtr=0, andOr, match;
    int[] numConds = new int[1];
    int rightSon=0;
    
    int posn = getPosn(partition, uptoPtr);

    while(quit == 0)
    {
      // get the rules from the KB
    
      ruleNum = readRule(partition, posn, text);

      newClause = getClause(text[0], 61);

      oldText = getRule(nodePtr);

      clauseNum = tree[nodePtr][1];

      oldClause = getClause(oldText, clauseNum);

      // match THEN clause against nodePtr clause
      match = pMatch(oldClause, newClause);

      // check results of match
      if(match == 1)
      {
        andOr = breakRule(text[0], numConds);
      
        rootPtr = buildSubtree(nodePtr, andOr, numConds[0], ruleNum);

        ++created[0];

        recreated = 1;
      }

      // join trees - append new subtree to existing tree
      if(firstTime == 1)
      {
        tree[nodePtr][4] = rootPtr;
        rightSon = rootPtr;
        firstTime = 0;
      }
      else
      {
        tree[rightSon][5] = rootPtr;
        rightSon = rootPtr;
      }

      recreated = 0;    

      posn = getPosn(partition, uptoPtr);

      // all rules read?
      if(posn == 0)
        quit = 1;
    }

    return rootPtr;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // process each general rule in UT_TABLE
  private void processUTTable() throws Exception
  {
    int partition = 2;
    int quit = 0;
    int[] uptoPtr = new int[1];  uptoPtr[0] = 0;
    int topNodePtr, ruleNum, andOr, result;
    int[] numConds = new int[1];
    String[] ruleText = new String[1];
   
    int posn = getPosn(partition, uptoPtr);
 
    // for each rule in 'general' partition
    while(quit == 0)
    {
      // build up the tree for this rule
      topNodePtr = treePtr;

      ruleNum = readRule(partition, posn, ruleText);

      andOr = breakRule(ruleText[0], numConds);

      // add the action clause
      toTree(ruleNum, 61, andOr, -1, 0, 0, 0);

      rootPtr = buildTree(topNodePtr, andOr, numConds[0], ruleNum);

      addSubTree(rootPtr, topNodePtr);

      result = parseTree(topNodePtr);

      // if proved, write to the STM
      if(result == 'T')
     {
        toSTM(topNodePtr, 'G');

        // write any NB to the STM
        anyNB(ruleText, partition);
      }

      posn = getPosn(partition, uptoPtr);

      // all rules read?
      if(uptoPtr[0] == 0)
        quit = 1;
    }

    for(int x=1;x<(UT_SIZE - 1);++x)
    {
      if(UT_TABLE[x][1] == 1 || UT_TABLE[x][1] == 3)
      {
        partition = UT_TABLE[x][0];
        quit = uptoPtr[0] = 0;
        posn = getPosn(partition, uptoPtr);

        // for each rule in partition
        while(quit == 0)
        {
          // build up tree for this rule
          topNodePtr = treePtr;

          ruleNum = readRule(partition, posn, ruleText);

          andOr = breakRule(ruleText[0], numConds);

          // add the action clause
          toTree(ruleNum, 61, andOr, -1, 0, 0, 0);

          rootPtr = buildTree(topNodePtr, andOr, numConds[0], ruleNum);

          addSubtree(rootPtr, topNodePtr);

          result = parseTree(topNodePtr);

          // if proved, write to STM
          if(result == 'T')
          {
            toSTM(topNodePtr, 'G');

            // write any NB to the STM
            anyNB(ruleText, partition);
          }

          posn = getPosn(partition, uptoPtr);

          // all rules read?
          if(uptoPtr[0] == 0)
            quit = 1;
        }
      }
      // else // == 2, ignore
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // creates new TREE entry
  private void toTree(int ruleNum, int clauseNum, int andOr, int father, int son, int brother, int stmNote) throws Exception
  {
    tree[treePtr][0] = ruleNum;
    tree[treePtr][1] = clauseNum;
    tree[treePtr][2] = andOr;
    tree[treePtr][3] = father;
    tree[treePtr][4] = son;
    tree[treePtr][5] = brother;
    tree[treePtr][6] = stmNote;

    ++treePtr;

    if(treePtr > treeSize)
    {
      System.out.println("error - tree full");
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // evaluate goal node
  private char parseTree(int nodePtr) throws Exception
  {
    char status = evaluateNode(nodePtr);

    int ruleNum, part, posn;
    String ruleText; //
    
    int satisfied = 0;
    while(satisfied == 0)
    {
      if(status == 'E') // tree has been extended
      {
        // update nodePtr to leaf
        nodePtr = nodeLeaf(nodePtr);
        status = evaluateNode(nodePtr);
      }
      else
      if(status == 'F') // node has been instantiated false
      {
        if(tree[nodePtr][2] == AND) // node is an AND)
        {
          nodePtr = nodeFather(nodePtr);
          
          if(tree[nodePtr][0] == 0 && 1==1) // nodePtr is to goal node)
            toSTM(nodePtr, 'F');

          if(1==1) // nodePtr is to goal node)
            satisfied = 1;
        }
        else 
        if(tree[nodePtr][2] == OR) // node is an OR
        {
          if(1==1) // nodePtr is to goal node)
            satisfied = 1;
          else
          {
            if(1==1) // there is another brother)
            {
              // update to brother
              nodePtr = nodeBrother(nodePtr);

              // update to leaf
              nodePtr = nodeLeaf(nodePtr);

              status = evaluateNode(nodePtr);
            }
            else // update to father
              nodePtr = nodeFather(nodePtr);

            if(tree[nodePtr][0] == 0 && 1==1) // nodePtr is a goal node)
              toSTM(nodePtr, 'F');
          }
        }      
      }
      else // status != 'F' // node has been instantiated true
      {
        if(1==1) // node is an AND)
        {
          if(1==1) // there is another brother)
          {
            // update to brother
            nodePtr = nodeBrother(nodePtr);
            status = evaluateNode(nodePtr);
          }
          else
          {
            // update to father
            nodePtr = nodeFather(nodePtr);
            toSTM(nodePtr, 'T');
            ruleNum = tree[nodePtr][0];
            part = rtmt[ruleNum][0];
            posn = rtmt[ruleNum][1];
            ruleText = readRule(part, posn);
            anyNB(ruleText, part);
       
            if(1==1) // if(nodePtr is to goal node)
              satisfied = 1;
          }
        }
        else // node is an OR
        {
          if(1==1) // nodePtr is to goal node)
            satisfied = 1;
          else
          {
            // update to father
            nodePtr = nodeFather(nodePtr);
     
            // make a note in the STM
            toSTM(nodePtr, 'T');
            ruleNum = tree[nodePtr][0];
            part = rtmt[ruleNum][0];
            posn = rtmt[ruleNum][1];
            ruleText = readRule(part, posn);
            anyNB(ruleText, part);
          }
        }          
      }
    }

    return status; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // to UT_TABLE the usability as appropriate
  private void writeUsability(String ruleText) throws Exception
  {
    // get text for THEN clause
    String clauseText = getClause(ruleText, 61);

    int row = getUTRow(clauseText.substring(1)); // getUTRow(clauseText[1...]);

    if(useType(clauseText.charAt(0)) == 70) // write 'Y' to appropriate usability column
      UT_TABLE[row][1] = 1;
    else // NOT_USE_TYPE (text[0] == 71) // write 'N' to appropriate usability column
      UT_TABLE[row][1] = 2;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // search partition in question then general partition
  private int srchPartitions(int nodePtr) throws Exception
  {
    int partition = getPartition(nodePtr);

    int topRoot = 0;
    int[] created = new int[1];  created[0] = 0;

    rootPtr = srchExtendTree(nodePtr, partition, topRoot, created);

    // if not general partition being looked-at
    if(partition != 2)
      rootPtr = srchExtendTree(nodePtr, 2, topRoot, created);

    return created[0]; 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // for a given rule
  private int buildSubtree(int nodePtr, int andOr, int numConds, int ruleNum) throws Exception
  {
    int brother;
    
    rootPtr = treePtr;

    // Add top 'link' branch

    toTree(0, 0, 0, treePtr, treePtr + 1, 0);

    for(int x=1;x<=numConds;++x) // for each condition
    {
      if(x == numConds)
        brother = 0;
      else brother = treePtr + 1;
 
      toTree(ruleNum, x, andOr, nodePtr, 0, brother, 0);
    }

    return rootPtr;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // for given rule
  private int buildTree(int nodePtr, int andOr, int numConds, int ruleNum) throws Exception
  {
    rootPtr = treePtr;
  
    int brother;
  
    for(int x=1;x<=numConds;++x)
    {
      if(x == numConds)
        brother = 0;
      else brother = treePtr + 1;
  
      toTree(ruleNum, x, andOr, nodePtr, 0, brother, 0);
    }
 
    return rootPtr;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // update NODE_PTR to to it's brother
  private int nodeBrother(int nodePtr) throws Exception
  {
    return tree[nodePtr][5];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // update NODE_PTR to it's father
  private int nodeFather(int nodePtr) throws Exception
  {
    return tree[nodePtr][3];
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // update NODE_PTR to it's leaf (eldest son)
  private int nodeLeaf(int nodePtr) throws Exception
  {
    int x = nodePtr;

    while(tree[x][4] != 0)
      x = tree[x][4];

    return x;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // join trees
  private void addSubtree(int rootPtr, int nodePtr) throws Exception  
  {
    tree[nodePtr][4] = rootPtr;

    tree[rootPtr][3] = nodePtr;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // check if LOOK_PTR is part of a tree
  private int treeAlive(int nodePtr, int lookPtr) throws Exception
  {
    // init
    int skip = 0;
    int endFlag = 0;
    int result = 0;
    int parsePtr = nodePtr;

    // set to goal node
    int x=0, z;
    while(x == 0)
    {
      z = tree[parsePtr][3];

      if(z == -1)
        x = 1;
      else parsePtr = z;
    }

    // at goal before we start
    if(lookPtr == parsePtr)
      result = 1;

    while(result == 0 && endFlag == 0)
    {
      if(skip == 0)
      {
        if(tree[parsePtr][4] > 0) // take STM path
          parsePtr = tree[parsePtr][4];
        else
        {
          if(tree[parsePtr][5] != 0) // take brother path
            parsePtr = tree[parsePtr][5];
          else // take father path
          {
            parsePtr = tree[parsePtr][3];
            skip =1;
          }
        }
      }
      else
      {
        if(tree[parsePtr][5] != 0) // take brother path
          parsePtr = tree[parsePtr][5];
        else // take father path
          parsePtr = tree[parsePtr][3];

        skip = 0;
      }

      if(lookPtr == parsePtr)
        result = 1;
      else
      {
        // reached goal node again?
        if(tree[parsePtr][3] == -1)
         endFlag = 1;
      }
    }

    return result;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // checks STM, checks KB, asks user
  private char evaluateNode(int nodePtr) throws Exception
  {
    // init
    char tOrFOrE = 'D';
    int result;
    int created=0;
    
    // try the STM for an answer
    if(tree[nodePtr][0] > 0)
    {
      result = srchSTM(nodePtr);

      if(result > 0) // node instantiated true
      {
        tree[nodePtr][4] = -result;
        tOrFOrE = 'T';
      }
      else
      if(result < 0) // node instantiated false;
      {
        tree[nodePtr][4] = result;
        tOrFOrE = 'F';
      }
    }

    // try the KB for an answer
    if(tOrFOrE == 'D')
    {
      if(tree[nodePtr][0] > 0) // search partitions
        created = srchPartitions(nodePtr);
      else
      {
        tree[nodePtr][0] = -tree[nodePtr][0];
        tOrFOrE = 'E';
      }
   
      if(created != 0)
        tOrFOrE = 'E';
    }

    // try the user for an answer
    if(tOrFOrE == 'D')
    {
      System.out.print("(");
      System.out.print("ST_COUNT");
      System.out.print(")");

//      ++ST_COUNT;
 
      char reply = ' '; //askEval(nodePtr);

      if(reply == 'T')
        tOrFOrE = 'T';
      else
      if(reply == 'F')
        tOrFOrE = 'F';
      else
      if(reply == 'D')
        tOrFOrE = 'F';
    
      toSTM(nodePtr, reply);
    }

    return tOrFOrE;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // determine position of LOOK_PTR relative to NODE_PTR
  private int posnParse(int nodePtr, int lookPtr) throws Exception
  {
    // init
    int result = 0;
    int skip = 0;
    int parsePtr = nodePtr;

    // current leaf
    if(nodePtr == lookPtr)
      result = 3;

    // if 'dead' tree
    if(nodePtr == 0)
      result = 1;

    // set to goal node
    if(result == 0)
    {
      int x = 0, z;
      while(x == 0)
      {
        z = tree[parsePtr][3];
        if(z == -1)
         x = 1;
         else parsePtr = z;
      }
    }

    while(result == 0)
    {
      if(skip == 0)
      {
        if(tree[parsePtr][4] > 0) // take son path
        {
          parsePtr = tree[parsePtr][4];
        }
        else
        {
          if(tree[parsePtr][5] != 0) // take brother path
            parsePtr = tree[parsePtr][5];
          else // take father path
          {
            parsePtr = tree[parsePtr][3];
            skip = 1;
          }
        }
      }
      else // == 1
      {
        if(tree[parsePtr][5] != 0) // take brother path
          parsePtr = tree[parsePtr][5];
        else // take father path
          parsePtr = tree[parsePtr][3];

        skip = 0;
      }

      // any match yet?
      if(parsePtr == nodePtr)
        result = 2;
      else // == lookPtr
        result = 1;
    }

    return result;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // find position of clause text with KNR_TYPES
  private int getUTRow(String clauseText) throws Exception
  {
    int match, result=0;
    String text;
    int quit = 0;
    int posn = 0;
    while(quit == 0)
    { 
      if(posn == UT_SIZE)
        quit = 1;
      else
      {
        text = getType(posn);
        match = pMatch(clauseText, text);
        if(match == 1)
          result = posn + 1;
        else quit = 1;
      }

      ++posn;
    }

    return result;
  }
   
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int getPosn(int partition, int[] uptoPtr) throws Exception
  {
     return 0;   
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int readRule(int partition, int posn, String[] text) throws Exception
  {
    return 0;
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getClause(String text, int clauseNum) throws Exception
  {
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getRule(int nodePtr) throws Exception
  {
    return "";
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private int pMatch(String oldClause, String newClause) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void toSTM(int nodePtr, char state) throws Exception          
  {
    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void anyNB(String ruleText, int partition) throws Exception
  {
    
  }
  
  private void anyNB(String[] ruleText, int partition) throws Exception
  {
    
  }
  
  private void addSubTree(int rootPtr, int topNodePtr) throws Exception
  {
  
  }

  private String readRule(int part, int posn) throws Exception
  {
    return "";    
  }

  private int useType(char ch) throws Exception
  {
    return 0;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  // search the STM for...
  private int srchSTM(int nodePtr) throws Exception
  {
    return 0;
  }
  
  private int getPartition(int nodePtr) throws Exception
  {
    return 0;
  }

  private void toTree(int a, int b, int c, int treePtr, int treePtr_1, int d) throws Exception
  {
    
  }

  private String getType(int posn) throws Exception
  {
    return "";    
  }

}
