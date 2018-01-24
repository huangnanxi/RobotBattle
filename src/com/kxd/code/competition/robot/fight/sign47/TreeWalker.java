package com.kxd.code.competition.robot.fight.sign47;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author mengqingyan 2017/12/6
 */
public class TreeWalker {

    public static <T extends TreeNode> boolean walkTreePreOrder(T root, TreeNodeVisitor<T> treeNodeVisitor) {
        boolean result = true;
        Map<T, Object> visitorContext = new HashMap<>();
        Stack<T> stack = new Stack<>();
        Stack<T> pStack = new Stack<>();
        T tmp;
        T tmpParent = null;
        stack.push(root);
        while (!stack.empty()) {
            if (!pStack.isEmpty()) {
                tmpParent = pStack.peek();
            }
            tmp = stack.pop();
            result = treeNodeVisitor.visitNode(tmp, tmpParent, visitorContext);
            if (!result) {
                return result;
            }
            if (tmpParent != null && getLastSon(tmpParent) == tmp) {
                pStack.pop();
            }
            List<T> sons = tmp.getSons();

            if (sons != null && sons.size() > 0) {
                for (int i = sons.size() - 1; i >= 0; i--) {
                    stack.push(sons.get(i));
                }
                pStack.push(tmp);

            }
        }

        return result;
    }

    private static TreeNode getLastSon(TreeNode parent) {
        List<TreeNode> sons = parent.getSons();
        if (sons == null || sons.size() == 0) {
            return null;
        }
        return sons.get(sons.size() - 1);
    }

    public interface TreeNodeVisitor<T extends TreeNode> {

        boolean visitNode(T treeNode, T parentTreeNode, Map<T, Object> visitorContext);
    }



    public static final TreeNodeVisitor NothingTreeNodeVisitor = new TreeNodeVisitor() {
        @Override
        public boolean visitNode(TreeNode treeNode, TreeNode parentTreeNode, Map visitorContext) {
            return true;
        }
    };
}
