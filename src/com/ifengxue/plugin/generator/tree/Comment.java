package com.ifengxue.plugin.generator.tree;

public class Comment extends AbstractElement {

  private String comment;
  private CommentType commentType = CommentType.FIELD;
  private Indent indent = Indent.TWO_SPACE;

  public Comment() {
  }

  public Comment(String comment, CommentType commentType, Indent indent) {
    this.comment = comment;
    this.commentType = commentType;
    this.indent = indent;
  }

  public static Comment newClassComment(String comment) {
    return new Comment(comment, CommentType.CLASS, Indent.TWO_SPACE);
  }

  public static Comment newFieldComment(String comment, Indent indent) {
    return new Comment(comment, CommentType.FIELD, indent);
  }

  public static Comment newMethodComment(String comment, Indent indent) {
    return new Comment(comment, CommentType.METHOD, indent);
  }

  @Override
  public String toString() {
    switch (commentType) {
      case CLASS:
        return "/**" + lineSeparator +
            " * " + comment + lineSeparator +
            " */";
      default:
        return "/**" + lineSeparator +
            indent.getIndent() + " * " + comment + lineSeparator +
            indent.getIndent() + " */";
    }

  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public CommentType getCommentType() {
    return commentType;
  }

  public void setCommentType(CommentType commentType) {
    this.commentType = commentType;
  }

  enum CommentType {CLASS, FIELD, METHOD}
}
