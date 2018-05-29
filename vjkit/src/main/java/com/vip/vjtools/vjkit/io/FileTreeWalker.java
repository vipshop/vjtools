package com.vip.vjtools.vjkit.io;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;
import com.vip.vjtools.vjkit.text.WildcardMatcher;

public class FileTreeWalker {

	/**
	 * 前序递归列出所有文件, 包含文件与目录，及根目录本身.
	 * 
	 * 前序即先列出父目录，在列出子目录. 如要后序遍历, 直接使用Files.fileTreeTraverser()
	 */
	public static List<File> listAll(File rootDir) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir).toList();
	}

	/**
	 * 前序递归列出所有文件, 只包含文件.
	 */
	public static List<File> listFile(File rootDir) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir).filter(Files.isFile()).toList();
	}

	/**
	 * 前序递归列出所有文件, 列出后缀名匹配的文件. （后缀名不包含.）
	 */
	public static List<File> listFileWithExtension(final File rootDir, final String extension) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir).filter(new FileExtensionFilter(extension)).toList();
	}

	/**
	 * 前序递归列出所有文件, 列出文件名匹配通配符的文件
	 * 
	 * 如 ("/a/b/hello.txt", "he*") 将被返回
	 */
	public static List<File> listFileWithWildcardFileName(final File rootDir, final String fileNamePattern) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir).filter(new WildcardFileNameFilter(fileNamePattern))
				.toList();
	}

	/**
	 * 前序递归列出所有文件, 列出文件名匹配正则表达式的文件
	 * 
	 * 如 ("/a/b/hello.txt", "he.*\.txt") 将被返回
	 */
	public static List<File> listFileWithRegexFileName(final File rootDir, final String regexFileNamePattern) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir)
				.filter(new RegexFileNameFilter(regexFileNamePattern)).toList();
	}

	/**
	 * 前序递归列出所有文件, 列出符合ant path风格表达式的文件
	 * 
	 * 如 ("/a/b/hello.txt", "he.*\.txt") 将被返回
	 */
	public static List<File> listFileWithAntPath(final File rootDir, final String antPathPattern) {
		return Files.fileTreeTraverser().preOrderTraversal(rootDir)
				.filter(new AntPathFilter(FilePathUtil.concat(rootDir.getAbsolutePath(), antPathPattern))).toList();
	}

	/**
	 * 直接使用Guava的TreeTraverser，获得更大的灵活度, 比如加入各类filter，前序/后序的选择，一边遍历一边操作
	 * 
	 * <pre>
	 * FileUtil.fileTreeTraverser().preOrderTraversal(root).iterator();
	 * </pre>
	 */
	public static TreeTraverser<File> fileTreeTraverser() {
		return Files.fileTreeTraverser();
	}

	/**
	 * 以文件名正则表达式为filter，配合fileTreeTraverser使用
	 */
	public static final class RegexFileNameFilter implements Predicate<File> {
		private final Pattern pattern;

		private RegexFileNameFilter(String pattern) {
			this.pattern = Pattern.compile(pattern);
		}

		@Override
		public boolean apply(File input) {
			return input.isFile() && pattern.matcher(input.getName()).matches();
		}
	}

	/**
	 * 以文件名通配符为filter，配合fileTreeTraverser使用.
	 * 
	 * @param pattern 支持*与?的通配符，如hello*.txt 匹配 helloworld.txt
	 */
	public static final class WildcardFileNameFilter implements Predicate<File> {
		private final String pattern;

		private WildcardFileNameFilter(String pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean apply(File input) {
			return input.isFile() && WildcardMatcher.match(input.getName(), pattern);
		}
	}

	/**
	 * 以文件名后缀做filter，配合fileTreeTraverser使用
	 */
	public static final class FileExtensionFilter implements Predicate<File> {
		private final String extension;

		private FileExtensionFilter(String extension) {
			this.extension = extension;
		}

		@Override
		public boolean apply(File input) {
			return input.isFile() && extension.equals(FileUtil.getFileExtension(input));
		}
	}

	/**
	 * 以ant风格的path为filter，配合fileTreeTraverser使用.
	 * 
	 * @param pattern 支持ant风格的通配符，如/var/?/a?.txt 匹配 /var/b/ab.txt, 其他通配符包括**,*
	 */
	public static final class AntPathFilter implements Predicate<File> {
		private final String pattern;

		private AntPathFilter(String pattern) {
			this.pattern = pattern;
		}

		@Override
		public boolean apply(File input) {
			return input.isFile() && WildcardMatcher.matchPath(input.getAbsolutePath(), pattern);
		}
	}
}
