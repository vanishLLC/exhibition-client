/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

package exhibition.module.impl.other;

import exhibition.Client;
import exhibition.event.Event;
import exhibition.event.RegisterEvent;
import exhibition.event.impl.EventPacket;
import exhibition.module.Module;
import exhibition.module.data.ModuleData;
import exhibition.module.data.settings.Setting;
import exhibition.util.Timer;
import exhibition.util.misc.ChatUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class KillSults extends Module {

    private PriorityQueue<String> chatQueue = new PriorityQueue();
    private Timer chatDelay = new Timer();

    private Setting shout = new Setting<>("SHOUT", false, "Will add /shout before your message to shout.");

    public KillSults(ModuleData data) {
        super(data);
        loadInsults();
        this.addSetting("SHOUT", shout);
    }

    public void resetTimer() {
        chatDelay.reset();
    }

    @RegisterEvent(events = {EventPacket.class})
    public void onEvent(Event event) {
        if(mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (insults.isEmpty())
            return;

        EventPacket ep = event.cast();
        if (chatDelay.delay(3250) && !chatQueue.isEmpty()) {
            chatDelay.reset();
            ((AutoGG) Client.getModuleManager().get(AutoGG.class)).resetTimer();
            ((AutoPlay) Client.getModuleManager().get(AutoPlay.class)).resetTimer();

            String message = chatQueue.poll();
            if (!message.equals("")) {
                ChatUtil.sendChat(message);
            }
        }
        Packet packet = ep.getPacket();
        if (packet instanceof S02PacketChat) {
            S02PacketChat packetChat = (S02PacketChat) packet;
            String unformatted = packetChat.getChatComponent().getUnformattedText();
            if (unformatted.contains("by " + mc.thePlayer.getName()) && !unformatted.contains("BED DESTRUCTION")) {
                String victim = unformatted.split(" ")[0];
                chatQueue.add(((boolean) shout.getValue() ? "/shout " : "") + getRandomInsult(victim));
            }
        }
    }

    public void loadInsults() {
        File saveFile = getInsultsFile();
        if (saveFile.exists()) {
            try {
                final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(saveFile), StandardCharsets.UTF_8));
                String str;
                while ((str = in.readLine()) != null) {
                    insults.add(str);
                }
                in.close();
            } catch (Exception e) {
            }
        } else {
            try {
                if (saveFile.createNewFile()) {
                    insults.addAll(Arrays.asList(getDefaultInsults()));
                    System.out.println("Created insults file successfully!");
                    final Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile), "UTF-8"));
                    for (String str : insults) {
                        out.write(str + System.getProperty("line.separator"));
                    }
                    out.close();
                    return;
                }
            } catch (Exception e) {
                return;
            }
        }
    }

    private File getInsultsFile() {
        File file = new File(Client.getDataDir().getAbsolutePath() + File.separator + "KillSults.txt");
        return file;
    }

    public String getRandomInsult(String name) {
        String insult = insults.get((int) (Math.random() * insults.size() - 1));
        return insult.replaceAll("%s", name);
    }

    private List<String> insults = new ArrayList<>();

    private String[] getDefaultInsults() {
        return new String[]{
                "Wow, you just died in a block game %s",
                "%s died in a block game lmfao.",
                "%s died for using an android device. LOL",
                "%s, your mother is of the homophobic type",
                "That's a #VictoryRoyale!, better luck next time, %s!",
                "%s, used Flux then got backhanded by the face of hypixel",
                "even loolitsalex has more wins then you %s",
                "my grandma plays minecraft better than you %s",
                "%s, you should look into purchasing vape",
                "Omg %s I'm so sorry",
                "%s, What's worse your skin or the fact your a casual f3ckin normie",
                "you know what %s, blind gamers deserve a chance too. I support you.",
                "that was a pretty bad move %s",
                "how does it feel to get stomped on %s",
                "%s, do you really like dying this much?",
                "if i had a choice between %s and jake paul, id choose jake paul",
                "hey %s, what does your IQ and kills have in common? They are both low af",
                "Hey %s, want some PvP advice?",
                "wow, you just died in a game about legos",
                "i'm surprised that you were able hit the 'Install' button %s",
                "%s I speak English not your gibberish.",
                "%s Take the L, kid",
                "%s got memed",
                "%s is a default skin!!!1!1!1!1!!1!1",
                "%s You died in a fucking block game",
                "%s likes anime",
                "%s Trash dawg, you barely even hit me.",
                "%s I just fucked him so hard he left the game",
                "%s get bent over and fucked kid",
                "%s couldn't even beat 4 block",
                "Someone get this kid a tissue, %s is about to cry!",
                "%s's dad is bald",
                "%s Your family tree must be a cactus because everybody on it is a prick.",
                "%s You're so fucking trash that the binman mistook you for garbage and collected you in the morning",
                "%s some kids were dropped at birth but you were clearly thrown at a wall",
                "%s go back to your mother's womb you retarded piece of shit",
                "Thanks for the free kill %s !",
                "Benjamin's forehead is bigger than your future Minecraft PvP career %s",
                "%s are you even trying?",
                "%s You. Are. Terrible.",
                "%s my mom is better at this game then you",
                "%s lololololol mad? lololololol",
                "%s /friend me so we can talk about how useless you are",
                "%s: \"Staff! Staff! Help me! I am dogcrap at this game and i am getting rekt!\"",
                "%s Is it really that hard to trace me while i'm hopping around you?",
                "%s, Vape is a cool thing you should look into!",
                "%s I'm not using reach, you just need to click faster.",
                "%s I hope you recorded that, so that you can watch how trash you really are.",
                "%s You have to use the left and right mouse button in this game, in case you forgot.",
                "%s I think that the amount of ping you have equates to your braincells dumbfuck asshat",
                "%s ALT+F4 to remove the problem",
                "%s alt+f4 for hidden perk window",
                "%s You'll eventually switch back to Fortnite again, so why not do it now?",
                "%s go back to fortnite where you belong, you degenerate 5 year old",
                "%s I'll be sure to Orange Justice the fucck out of your corpse",
                "%s Exhibob better than you!1",
                "%s I'm a real gamer, and you just got owned!!",
                "%s Take a taste of your own medicine you clapped closet cheater",
                "%s go drown in your own salt",
                "%s go and suck off prestonplayz, you 7 yr old fanboy",
                "%s how are you so bad. I'm losing brain cells just watching you play",
                "%s Jump down from your school building with a rope around your neck.",
                "%s dominated, monkey :dab:",
                "%s Please add me as a friend so that you can shout at me. I live for it.",
                "%s i fvcked your dad",
                "%s Yeah, I dare you, rage quit. Come on, make us both happy.",
                "%s No, you are not blind! I DID own you!",
                "%s easy 10 hearted L",
                "%s It's almost as if i can hear you squeal from the other side!",
                "%s If you read this, you are confirmed homosexual",
                "%s have you taken a dump lately? Because I just beat the shit of out you.",
                "%s 6 block woman beater",
                "%s feminist demolisher",
                "%s chromosome count doubles the size of this game",
                "a million years of evolution and we get %s",
                "if the body is 70 percent water how is %s 100 percent salt???",
                "%s L",
                "%s got rekt",
                "%s you're so fat that when you had a fire in your house you dialled 999 on the microwave",
                "LMAO %s is a Fluxuser",
                "LMAO %s is a Sigmauser",
                "%s I suffer from these fukking kicks, grow brain lol",
                "LMAO %s a crack user",
                "%s Hypixel thought could stop us from cheating, huh, you are just as delusional as him",
                "%s GET FUCKED IM ON BADLION CLIENT WHORE",
                "%s should ask tene if i was hacking or not",
                "%s check out ARITHMOS CHANNEL",
                "%s gay",
                "%s, please stop",
                "%s, I play fortnite duos with your mom",
                "%s acts hard but %s's dad beats him harder",
                "Lol commit not alive %s",
                "How'd you hit the DOWNLOAD button with that aim? %s",
                "I'd say your aim is cancer, but at least cancer kills people. %s",
                "%s is about as useful as pedals on a wheelchair",
                "%s's aim is now sponsored by Parkinson's!",
                "%s, I'd say uninstall but you'd probably miss that too.",
                "%s, I bet you edate.",
                "%s, you probably watch tenebrous videos and are intruiged",
                "%s Please could you not commit not die kind sir thanks",
                "%s gay",
                "%s you probably suck on door knobs",
                "%s go commit stop breathing u dumb idot",
                "%s go commit to sucking on door knobs",
                "the only way you can improve at pvp %s is by taking a long walk off a short pier",
                "L %s",
                "%s Does not have a good client",
                "%s's client refused to work",
                "%s Stop hacking idiot",
                "%s :potato:",
                "%s go hunt kangaroos fucking aussie ping",
                "%s Super Mario Bros. deathsound",
                "Hey everyone, do /friend add %s , and tell them how trash they are",
                "%s Just do a France 1940, thank you",
                "Hey %s , would you like to hear a joke? Yeah, you ain't getting any",
                "%s got OOFed",
                "You mum your dad the ones you never had %s",
                "%s please be toxic to me, I enjoy it",
                "oof %s",
                "%s knock knock, FBI open up, we saw you searched for cracked vape.",
                "%s plez commit jump out of window for free rank",
                "%s you didn't even stand a chance!",
                "%s keep trying!",
                "%s, you're the type of player to get 3rd place in a 1v1",
                "%s, I'm not saying you're worthless, but I would unplug your life support to charge my phone",
                "I didn't know dying was a special ability %s",
                "%s, Stephen Hawking had better hand-eye coordination than you",
                "%s, kids like you were the inspiration for birth control",
                "%s you're the definition of bane",
                "%s lol GG!!!",
                "%s lol bad client what is it exhibition?",
                "%s L what are you lolitsalex?",
                "%s gg e z kid",
                "%s tene is my favorite youtuber and i bought his badlion client clock so i'm legit",
                "Don't forget to report me %s",
                "Your IQ is that of a Steve %s",
                "%s have you taken a dump lately? Because I just beat the shit of out you.",
                "%s dont ever put bean in my donut again.",
                "%s 2 plus 2 is 4, minus 1 that's your IQ",
                "I think you need vape %s !",
                "%s You just got oneTapped LUL",
                "%s You're the inspiration for birth control",
                "%s I don't understand why condoms weren't named by you.",
                "%s, My blind grandpa has better aim than you.",
                "%s, Exhibob better then you!",
                "%s, u r So E.Z",
                "Exhibition > %s",
                "%s, NMSL",
                "%s, your parents abondoned you, then the orphanage did the same",
                "%s,stop using trash client like sigma.",
                "%s, your client is worse than sigma, and that's an achievement",
                "%s, ur fatter than Napoleon",
                "%s please consider not alive",
                "%s, probably bought sigma premium",
                "%s, probably asks for sigma premium keys",
                "%s the type of person to murder someone and apologize saying it was a accident",
                "%s you're the type of person who would quickdrop irl",
                "%s, got an F on the iq test.",
                "Don't forget to report me %s",
                "%s even viv is better than you LMAO",
                "%s your mom gaye",
                "%s I Just Sneezed On Your Forehead",
                "%s your teeth are like stars - golden, and apart.",
                "%s Rose are blue, stars are red, you just got hacked on and now you're dead",
                "%s i don't hack because watchdog is watching so it would ban me anyway.",
                "%s, chill out on the paint bro",
                "%s You got died from the best client in the game, now with Infinite Sprint bypass",
                "%s you're so fat, that your bellybutton reaches your house 20 minutes before you do",
                "%s your dick is so small, that you bang cheerios"
        };
    }

}
