
@Override
public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
    if (response.isSuccessful()) {
        java.io.BufferedReader reader = new java.io.BufferedReader(response.body().charStream());
        StringBuilder result = new StringBuilder();
        StringBuilder rawResponse = new StringBuilder(); // 记录原始响应

        String line;
        int lineCount = 0;
        try {
            while ((line = reader.readLine()) != null) {
                lineCount++;
                rawResponse.append(line).append("\n");
                System.out.println("第" + lineCount + "行: " + line);

                // 处理以data: 开头的行
                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6);
                    System.out.println("  数据内容: " + jsonData);

                    // 检查是否为结束标记
                    if (jsonData.trim().equals("[DONE]")) {
                        System.out.println("  收到结束标记 [DONE]");
                        break; // 结束循环
                    }

                    try {
                        JsonObject jsonObject = new JsonParser().parse(jsonData).getAsJsonObject();
                        System.out.println("  解析的JSON: " + jsonObject.toString());

                        JsonArray choices = jsonObject.getAsJsonArray("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonObject firstChoice = choices.get(0).getAsJsonObject();
                            System.out.println("  Choice对象: " + firstChoice.toString());

                            if (firstChoice.has("delta")) {
                                JsonObject delta = firstChoice.getAsJsonObject("delta");
                                System.out.println("  Delta对象: " + delta.toString());

                                if (delta.has("content")) {
                                    JsonElement contentElement = delta.get("content");
                                    if (!contentElement.isJsonNull()) {
                                        String content = contentElement.getAsString();
                                        System.out.println("  Content内容: " + content);

                                        if (!content.isEmpty()) {
                                            result.append(content);
                                            System.out.println("  累积内容长度: " + result.length());

                                            // 在主线程更新UI
                                            final String currentResult = result.toString();
                                            runOnUiThread(() -> {
                                                tvResult.setText(currentResult);
                                            });
                                        }
                                    } else {
                                        System.out.println("  Content为null，检查reasoning_content...");
                                    }
                                } else {
                                    System.out.println("  Delta中没有content字段，检查reasoning_content...");
                                }

                                if (delta.has("reasoning_content")) {
                                    JsonElement reasoningContentElement = delta.get("reasoning_content");
                                    if (!reasoningContentElement.isJsonNull()) {
                                        String reasoningContent = reasoningContentElement.getAsString();
                                        System.out.println("  Reasoning Content内容: " + reasoningContent);

                                        if (!reasoningContent.isEmpty()) {
                                            result.append(reasoningContent);
                                            System.out.println("  累积内容长度: " + result.length());

                                            // 在主线程更新UI
                                            final String currentResult = result.toString();
                                            runOnUiThread(() -> {
                                                tvResult.setText(currentResult);
                                            });
                                        }
                                    } else {
                                        System.out.println("  Reasoning_content为null");
                                    }
                                } else {
                                    System.out.println("  Delta中没有reasoning_content字段");
                                }
                            } else {
                                System.out.println("  Choice中没有delta字段");
                            }
                        } else {
                            System.out.println("  Choices为空或大小为0");
                        }
                    } catch (Exception e) {
                        // JSON解析失败，记录错误但继续处理
                        System.out.println("  JSON解析错误: " + e.getMessage());
                        System.out.println("  解析失败的数据: " + jsonData);
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("  非data行，忽略");
                }
            }
        } finally {
            reader.close();
            System.out.println("=== 响应处理完成 ===");
            System.out.println("总共处理了 " + lineCount + " 行");
            System.out.println("原始响应内容:\n" + rawResponse.toString());
        }

        // 显示最终结果
        final String finalResult = result.toString();
        System.out.println("=== 最终结果 ===");
        System.out.println("累积内容长度: " + finalResult.length());
        System.out.println("最终内容: " + finalResult);

        runOnUiThread(() -> {
            if (finalResult.isEmpty()) {
                tvResult.setText("API调用完成但未生成内容\n\n提示词:\n" + prompt);
            } else {
                tvResult.setText(finalResult);
            }
        });
    } else {
    }
}
