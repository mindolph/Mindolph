package com.mindolph.core.llm;

import com.mindolph.core.constant.GenAiModelProvider;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * @since unknown
 */
public class AgentMeta implements Serializable {
   private String id;
   private String name;
   private GenAiModelProvider provider;
   private ModelMeta chatModel;
   private String promptTemplate;
   private List<File> files;

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public GenAiModelProvider getProvider() {
      return provider;
   }

   public void setProvider(GenAiModelProvider provider) {
      this.provider = provider;
   }

   public ModelMeta getChatModel() {
      return chatModel;
   }

   public void setChatModel(ModelMeta chatModel) {
      this.chatModel = chatModel;
   }

   public String getPromptTemplate() {
      return promptTemplate;
   }

   public void setPromptTemplate(String promptTemplate) {
      this.promptTemplate = promptTemplate;
   }

   public List<File> getFiles() {
      return files;
   }

   public void setFiles(List<File> files) {
      this.files = files;
   }
}
