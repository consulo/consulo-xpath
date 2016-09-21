/*
 * Copyright 2005 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.lang.xpath;

import org.intellij.lang.xpath.psi.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.lang.LanguageVersion;

public class XPathParserDefinition implements ParserDefinition
{

	@Override
	@NotNull
	public Lexer createLexer(@NotNull LanguageVersion languageVersion)
	{
		return XPathLexer.create(false);
	}

	@Override
	@NotNull
	public IFileElementType getFileNodeType()
	{
		return XPathElementTypes.FILE;
	}

	@Override
	@NotNull
	public TokenSet getWhitespaceTokens(@NotNull LanguageVersion languageVersion)
	{
		return TokenSet.create(XPathTokenTypes.WHITESPACE);
	}

	@Override
	@NotNull
	public TokenSet getCommentTokens(@NotNull LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@Override
	@NotNull
	public TokenSet getStringLiteralElements(@NotNull LanguageVersion languageVersion)
	{
		return TokenSet.create(XPathTokenTypes.STRING_LITERAL);
	}

	@Override
	@NotNull
	public PsiParser createParser(@NotNull LanguageVersion languageVersion)
	{
		return new XPathParser();
	}

	@NotNull
	@Override
	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
	{
		return SpaceRequirements.MUST_NOT;
	}

	@Override
	@NotNull
	public final PsiElement createElement(@NotNull ASTNode node)
	{
		final IElementType type = node.getElementType();

		final PsiElement element = createElement(type, node);
		if(element != null)
		{
			return element;
		}
		return new XPathTokenImpl(node);
	}

	@Nullable
	protected PsiElement createElement(IElementType type, ASTNode node)
	{
		if(type == XPathElementTypes.NUMBER)
		{
			return new XPathNumberImpl(node);
		}
		else if(type == XPathElementTypes.STRING)
		{
			return new XPathStringImpl(node);
		}
		else if(type == XPathElementTypes.BINARY_EXPRESSION)
		{
			return new XPathBinaryExpressionImpl(node);
		}
		else if(type == XPathElementTypes.PREFIX_EXPRESSION)
		{
			return new XPathPrefixExpressionImpl(node);
		}
		else if(type == XPathElementTypes.PARENTHESIZED_EXPR)
		{
			return new XPathParenthesizedExpressionImpl(node);
		}
		else if(type == XPathElementTypes.FILTER_EXPRESSION)
		{
			return new XPathFilterExpressionImpl(node);
		}
		else if(type == XPathElementTypes.FUNCTION_CALL)
		{
			return new XPathFunctionCallImpl(node);
		}
		else if(type == XPathElementTypes.AXIS_SPECIFIER)
		{
			return new XPathAxisSpecifierImpl(node);
		}
		else if(type == XPathElementTypes.PREDICATE)
		{
			return new XPathPredicateImpl(node);
		}
		else if(type == XPathElementTypes.LOCATION_PATH)
		{
			return new XPathLocationPathImpl(node);
		}
		else if(type == XPathElementTypes.STEP)
		{
			return new XPathStepImpl(node);
		}
		else if(type == XPathElementTypes.NODE_TEST)
		{
			return new XPathNodeTestImpl(node);
		}
		else if(type == XPathElementTypes.NODE_TYPE)
		{
			return new XPathNodeTypeTestImpl(node);
		}
		else if(type == XPathElementTypes.VARIABLE_REFERENCE)
		{
			return new XPathVariableReferenceImpl(node);
		}
		else if(type == XPathElementTypes.EMBEDDED_CONTENT)
		{
			return new XPathEmbeddedContentImpl(node);
		}

		return null;
	}

	@Override
	public PsiFile createFile(@NotNull FileViewProvider viewProvider)
	{
		return new XPathFile(viewProvider, XPathFileType.XPATH);
	}
}
