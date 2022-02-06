package com.github.simulatan.countvotes;

import com.github.simulatan.countvotes.utils.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jcurses.event.ItemEvent;
import jcurses.event.ValueChangedEvent;
import jcurses.system.InputChar;
import jcurses.system.Toolkit;
import jcurses.widgets.*;
import marcono1234.gson.recordadapter.RecordTypeAdapterFactory;

import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.simulatan.countvotes.utils.VotesManager.*;

public class Main {

	private static Window window;
	private static ListWidget scores;
	private static ListWidget searchResultsList;
	private static ListWidget recentActions;
	private static TextField searchCandidateField;

	public static final Gson GSON = new GsonBuilder()
			.registerTypeAdapterFactory(RecordTypeAdapterFactory.DEFAULT)
			.enableComplexMapKeySerialization()
			.create();

	public static void main(String[] args) {
		log("Starting...");
		System.setOut(new LogPrintStream(System.out));
		System.setErr(new LogPrintStream(System.err));

		VotesManager.loadVotes();

		int height = Toolkit.getScreenHeight() / 16 * 15;
		int width = Toolkit.getScreenWidth() / 16 * 15;

		try {
			Field defaultClosingChar = Window.class.getDeclaredField("__defaultClosingChar");
			defaultClosingChar.setAccessible(true);
			defaultClosingChar.set(Window.class, new InputChar(InputChar.KEY_F4));
		} catch (Exception e) {
			log(e);
		}

		window = new Window(width, height, true, "Count Votes");
		DefaultLayoutManager mgr = new DefaultLayoutManager();
		mgr.bindToContainer(window.getRootPanel());

		scores = new ListWidget();
		scores.setFocusable(false);
		scores.setSelectable(false);
		scores.add("Total votes go here...");

		recentActions = new ListWidget();
		recentActions.setSelectable(true);
		recentActions.addListener(event -> showContextMenu(height, width, event));

		final Pattern validCandidateNames = Pattern.compile("^[a-zA-Z0-9_\\-\\sÄäÖöÜüß]*$", Pattern.CASE_INSENSITIVE);
		searchCandidateField = new TextField() {
			@Override
			protected boolean handleInput(InputChar ch) {
				if (ch.isSpecialCode()) return super.handleInput(ch);

				if (validCandidateNames.matcher(Character.toString(ch.getCharacter())).matches())
					return super.handleInput(ch);

				return false;
			}
		};

		final Pattern newCandidatePattern = Pattern.compile("^.*\"(.+)\".*$", Pattern.MULTILINE);
		searchResultsList = new ListWidget();
		searchResultsList.setSelectable(false);
		searchResultsList.add("Type something to search");
		searchResultsList.addListener(val -> {
			Matcher matcher;
			String candidateName;
			if (val.getItem() instanceof String s && (matcher = newCandidatePattern.matcher(s)).matches()) {
				candidateName = matcher.group(1);
				searchResultsList.add(candidateName);
				searchResultsList.remove(s);
			} else
				candidateName = (String) val.getItem();
			Vote vote = new Vote(Candidate.of(candidateName));
			addVote(vote);
			recentActions.add(0, vote.getFormatted());
			searchCandidateField.setText("");
			updateVoteCount();
			// TODO: fix
			searchCandidateField.getFocus();
		});

		searchCandidateField.getFocus();
		searchCandidateField.addListener(Main::updateSearchResults);

		mgr.addWidget(searchCandidateField, 40, 1, width - 82, height / 5, WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_CENTER);
		mgr.addWidget(searchResultsList, 40, 2, width - 82, height - 4, WidgetsConstants.ALIGNMENT_TOP, WidgetsConstants.ALIGNMENT_CENTER);
		mgr.addWidget(scores, 0, 0, 40, height - 2, WidgetsConstants.ALIGNMENT_CENTER, WidgetsConstants.ALIGNMENT_LEFT);
		mgr.addWidget(recentActions, width - 42, 0, 40, height - 2, WidgetsConstants.ALIGNMENT_CENTER, WidgetsConstants.ALIGNMENT_RIGHT);
		initRecentActions();
		updateSearchResults("");
		updateVoteCount();
		window.show();
		Runtime.getRuntime().addShutdownHook(new Thread(window::close));
	}

	private static void showVoteInfo(int height, int width, ItemEvent event, Vote vote) {
		List<String> lines = Arrays.asList(
				"Candidate: " + vote.candidate().getName(),
				"Time: " + vote.getTimeFormatted(),
				"Time (ms): " + vote.time(),
				"ID: " + vote.id(),
				"Back"
		);
		PopUpMenu info = new PopUpMenu(width / 2, height / 2 - 3, "Vote information");
		lines.forEach(info::add);
		while (true) {
			info.show();
			String selectedItem = info.getSelectedItem();

			if (selectedItem.equalsIgnoreCase("Back")) {
				break;
			} else if (selectedItem.startsWith("Candidate: ")) {
				PopUpMenu candidateInfo = new PopUpMenu(width / 2, height / 2 - 3, "Candidate information");
				candidateInfo.add("Name: " + vote.candidate().getName());
				List<Vote> votes = getVotes(vote.candidate());
				candidateInfo.add("Vote Count: " + votes.size());
				if (votes.size() > 0) {
					candidateInfo.add("Last Vote @ " + votes.get(votes.size() - 1).getTimeFormatted());
				} else {
					candidateInfo.add("No votes yet.");
				}
				candidateInfo.add("Back");
				while (true) {
					candidateInfo.show();
					String candidateItem = candidateInfo.getSelectedItem();
					if (candidateItem.equalsIgnoreCase("Back")) {
						break;
					}
					java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(candidateItem), null);
				}
			} else {
				java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(selectedItem), null);
			}
		}
		showContextMenu(height, width, event);
	}

	private static void showContextMenu(int height, int width, ItemEvent event) {
		PopUpMenu menu = new PopUpMenu(width / 2 + 5, height / 2 - 2, "Context Actions");
		menu.add("Delete");
		menu.add("Show information");
		menu.add("Cancel");
		menu.show();
		String item = (String) event.getItem();
		Vote vote = getVote(item);
		if (menu.getSelectedItem().equals("Delete")) {
			if (vote != null) {
				PopUpMenu confirm = new PopUpMenu(width / 2 + 5, height / 2 - 2, "Delete vote?");
				confirm.add("Confirm Deletion");
				confirm.add("Don't delete");
				confirm.show();
				if (confirm.getSelectedItem().equals("Confirm Deletion")) {
					removeVote(vote);
					menu.remove(item);
					updateVoteCount();
				} else {
					showContextMenu(height, width, event);
				}
			} else {
				log("Vote not found (formatted: " + event.getItem() + ")\n " + votes);
			}
		} else if (menu.getSelectedItem().equalsIgnoreCase("Show information")) {
			log("Showing info for: " + vote);
			if (vote != null) {
				showVoteInfo(height, width, event, vote);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void updateVoteCount() {
		scores.clear();
		scores.add("Total votes: " + votes.values().stream().mapToInt(java.util.List::size).sum());
		// add votes to score list, sorted by votes
		votes
			.entrySet()
			.stream()
			.sorted(Comparator.comparingInt(e -> ((Map.Entry<Candidate, java.util.List<Vote>>) e).getValue().size()).reversed())
			.forEach(e -> scores.add(e.getKey().getName() + ": " + e.getValue().size()));
		recentActions.clear();
		Utils.reverse(recentVotes.stream()
				.map(Vote::getFormatted))
				.forEach(recentActions::add);
		if (recentActions.getItemsCount() == 0) {
			recentActions.add("No recent actions");
			recentActions.setSelectable(false);
			recentActions.setFocusable(false);
			searchCandidateField.getFocus();
		} else {
			recentActions.setFocusable(true);
		}
		window.show();
	}

	private static void updateSearchResults(ValueChangedEvent val) {
		updateSearchResults(((TextField) val.getSource()).getText());
	}

	private static void updateSearchResults(String newValue) {
		searchResultsList.clear();
		votes.getCandidatesStartingWith(newValue).forEach(searchResultsList::add);

		if (!newValue.isEmpty() && !votes.containsKey(Candidate.of(newValue)))
			searchResultsList.add("Add new candidate with name \"" + newValue + "\"");

		window.show();
	}

	private static void initRecentActions() {
		recentVotes.clear();
		List<Vote> e = votes.values().stream()
				.flatMap(List::stream)
				.sorted(Comparator.comparingLong(Vote::time))
				.toList();
		recentVotes.addAll(e);
		Vote.nextId = e.stream().max(Comparator.comparingLong(Vote::id)).map(Vote::id).orElse(0) + 1;
	}

	private static Vote getVote(String name) {
		return recentVotes.stream()
				.filter(v -> v.id() == Integer.parseInt(name.split(" ")[0].substring(1)))
				.findFirst()
				.orElse(null);
	}

	private static final Path LOGFILE;

	static {
		File LOGFILE_AS_FILE = new File("countvotes.log");
		LOGFILE = LOGFILE_AS_FILE.toPath();
		try {
			//noinspection ResultOfMethodCallIgnored
			LOGFILE_AS_FILE.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(Object o) {
		log(o.toString());
	}

	public static void log(String text) {
		try {
			Files.writeString(LOGFILE, text + "\n", StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}